<template>
  <a-layout class="app-layout">
    <AppHeader />

    <a-layout-content class="content-wrap">
      <section class="page-title-row">
        <div>
          <h1>用户管理</h1>
          <p>按用户名查询用户信息，管理员可删除用户。权限和登录态以后端 Session 为准。</p>
        </div>
        <a-space>
          <a-button :loading="loading" @click="fetchUsers">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </section>

      <section class="current-user-panel">
        <a-descriptions size="small" :column="{ xs: 1, sm: 2, md: 4 }" bordered>
          <a-descriptions-item label="当前用户">
            {{ userStore.currentUser?.userAccount || '页面刷新后无法自动恢复' }}
          </a-descriptions-item>
          <a-descriptions-item label="用户 ID">
            {{ userStore.currentUser?.id || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="角色">
            {{ formatRole(userStore.currentUser?.userRole) }}
          </a-descriptions-item>
          <a-descriptions-item label="登录态">
            {{ userStore.currentUser ? '已记录到前端状态' : '仅以后端 Session 为准' }}
          </a-descriptions-item>
        </a-descriptions>
      </section>

      <section v-if="userStore.isAdmin" class="toolbar">
        <a-space wrap>
          <a-button type="primary" :loading="codeGenerating" @click="handleGenerateRegisterCode">
            <template #icon>
              <GiftOutlined />
            </template>
            生成注册码
          </a-button>
          <a-typography-text v-if="generatedCode" code copyable>
            {{ generatedCode }}
          </a-typography-text>
          <a-input-search
            v-model:value="codeToCheck"
            class="register-code-check"
            placeholder="输入注册码后校验"
            enter-button="校验"
            allow-clear
            :loading="codeChecking"
            @search="handleCheckRegisterCode"
          />
          <a-tag v-if="codeCheckResult === true" color="success">可用</a-tag>
          <a-tag v-else-if="codeCheckResult === false" color="error">不可用</a-tag>
        </a-space>
      </section>

      <section class="toolbar">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="按用户名搜索"
          enter-button="搜索"
          allow-clear
          :loading="loading"
          @search="fetchUsers"
        />
      </section>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="users"
        :loading="loading"
        :pagination="{ pageSize: 8, showSizeChanger: false }"
        class="user-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatarUrl'">
            <a-avatar :src="record.avatarUrl || undefined" :size="36">
              {{ getAvatarText(record) }}
            </a-avatar>
          </template>

          <template v-else-if="column.key === 'gender'">
            {{ formatGender(record.gender) }}
          </template>

          <template v-else-if="column.key === 'userRole'">
            <a-tag :color="record.userRole === 1 ? 'green' : 'blue'">
              {{ formatRole(record.userRole) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'userStatus'">
            <a-tag :color="record.userStatus === 0 || record.userStatus == null ? 'success' : 'warning'">
              {{ formatStatus(record.userStatus) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-popconfirm
              title="确认删除这个用户吗？"
              ok-text="删除"
              cancel-text="取消"
              ok-type="danger"
              @confirm="handleDelete(record.id)"
            >
              <a-button danger size="small" :disabled="!record.id">
                <template #icon>
                  <DeleteOutlined />
                </template>
                删除
              </a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import type { TableColumnsType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import { DeleteOutlined, GiftOutlined, ReloadOutlined } from '@ant-design/icons-vue';

import { checkRegisterCode, deleteUser, generateRegisterCode, searchUsers } from '@/api/user';
import AppHeader from '@/components/AppHeader.vue';
import { useUserStore } from '@/stores/user';
import type { User } from '@/types/user';

const userStore = useUserStore();
const loading = ref(false);
const codeGenerating = ref(false);
const codeChecking = ref(false);
const searchKeyword = ref('');
const users = ref<User[]>([]);
const generatedCode = ref('');
const codeToCheck = ref('');
const codeCheckResult = ref<boolean | null>(null);

const columns: TableColumnsType<User> = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '头像', dataIndex: 'avatarUrl', key: 'avatarUrl', width: 88 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', ellipsis: true },
  { title: '用户名', dataIndex: 'username', key: 'username', ellipsis: true },
  { title: '邮箱', dataIndex: 'email', key: 'email', ellipsis: true },
  { title: '手机号', dataIndex: 'phone', key: 'phone', ellipsis: true },
  { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 110 },
  { title: '状态', dataIndex: 'userStatus', key: 'userStatus', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 110, fixed: 'right' },
];

onMounted(() => {
  void fetchUsers();
});

async function fetchUsers() {
  loading.value = true;
  try {
    const result = await searchUsers(searchKeyword.value.trim());
    if (!Array.isArray(result)) {
      users.value = [];
      message.warning('未登录、无管理员权限，或后端未返回用户列表');
      return;
    }

    users.value = result;
  } finally {
    loading.value = false;
  }
}

async function handleDelete(id?: number) {
  if (!id) {
    message.error('用户 ID 无效');
    return;
  }

  const success = await deleteUser(id);
  if (!success) {
    message.error('删除失败，请确认管理员权限');
    return;
  }

  message.success('删除成功');
  await fetchUsers();
}

async function handleGenerateRegisterCode() {
  codeGenerating.value = true;
  try {
    const code = await generateRegisterCode();
    if (!code) {
      message.error('生成失败，请确认当前账号是管理员且后端 Session 有效');
      return;
    }

    generatedCode.value = code;
    codeToCheck.value = code;
    codeCheckResult.value = null;
    message.success('注册码生成成功');
  } finally {
    codeGenerating.value = false;
  }
}

async function handleCheckRegisterCode() {
  const code = codeToCheck.value.trim();
  codeCheckResult.value = null;
  if (!code) {
    message.warning('请先输入注册码');
    return;
  }
  if (!/^[a-zA-Z0-9]{12}$/.test(code)) {
    message.warning('注册码必须是 12 位大小写字母或数字');
    return;
  }

  codeChecking.value = true;
  try {
    const available = await checkRegisterCode(code);
    codeCheckResult.value = available;
    if (available) {
      message.success('注册码可用');
      return;
    }

    message.error('注册码无效或已被使用');
  } finally {
    codeChecking.value = false;
  }
}

function formatGender(gender?: number | null) {
  if (gender === 0) return '女';
  if (gender === 1) return '男';
  return '-';
}

function formatRole(role?: number | null) {
  return role === 1 ? '管理员' : '普通用户';
}

function formatStatus(status?: number | null) {
  return status === 0 || status == null ? '正常' : `状态 ${status}`;
}

function formatDate(value?: string | null) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString();
}

function getAvatarText(user: User) {
  return (user.username || user.userAccount || 'U').slice(0, 1).toUpperCase();
}
</script>
