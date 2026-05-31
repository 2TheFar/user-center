<template>
  <a-layout class="app-layout">
    <AppHeader />

    <a-layout-content class="content-wrap">
      <section class="page-title-row">
        <div>
          <h1>用户管理</h1>
          <p>查看用户信息、维护账号状态，并为新成员发放邀请码。</p>
        </div>
        <a-space wrap>
          <a-button v-if="!userStore.currentUser" @click="goLogin">
            <template #icon>
              <LoginOutlined />
            </template>
            登录
          </a-button>
          <a-button :loading="loading" @click="fetchUsers">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </section>

      <section class="dashboard-grid" aria-label="用户中心概览">
        <div class="metric-card">
          <span>用户总数</span>
          <strong>{{ users.length }}</strong>
          <small>当前列表中的用户数量</small>
        </div>
        <div class="metric-card">
          <span>管理员</span>
          <strong>{{ adminCount }}</strong>
          <small>负责用户维护的账号</small>
        </div>
        <div class="metric-card">
          <span>普通用户</span>
          <strong>{{ normalUserCount }}</strong>
          <small>正常使用平台的账号</small>
        </div>
      </section>

      <section class="current-user-panel">
        <div class="current-user-main">
          <a-avatar :src="resolveAvatarSrc(userStore.currentUser?.avatarUrl) || undefined" :size="48">
            {{ getAvatarText(userStore.currentUser || {}) }}
          </a-avatar>
          <div>
            <h2>{{ currentUserName }}</h2>
            <p>{{ currentUserHint }}</p>
          </div>
        </div>
        <a-descriptions size="small" :column="{ xs: 1, sm: 2, md: 4 }">
          <a-descriptions-item label="用户 ID">
            {{ userStore.currentUser?.id || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="账号">
            {{ userStore.currentUser?.userAccount || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="角色">
            <a-tag :color="userStore.isAdmin ? 'green' : 'blue'">
              {{ formatRole(userStore.currentUser?.userRole) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="登录状态">
            {{ userStore.currentUser ? '已登录' : '未登录' }}
          </a-descriptions-item>
        </a-descriptions>
      </section>

      <section class="workspace-panel">
        <div class="panel-heading">
          <div>
            <h2>查找用户</h2>
            <p>输入用户名可以快速定位用户，留空则查看全部用户。</p>
          </div>
        </div>
        <div class="toolbar-row">
          <a-input-search
            v-model:value="searchKeyword"
            placeholder="按用户名搜索"
            enter-button="搜索"
            allow-clear
            :loading="loading"
            @search="fetchUsers"
          />
          <a-button @click="resetSearch">清空</a-button>
        </div>
      </section>

      <section v-if="userStore.isAdmin" class="workspace-panel">
        <div class="panel-heading">
          <div>
            <h2>邀请码</h2>
            <p>为新成员生成邀请码，也可以检查邀请码是否仍可使用。</p>
          </div>
        </div>
        <div class="toolbar-row register-code-row">
          <a-button type="primary" :loading="codeGenerating" @click="handleGenerateRegisterCode">
            <template #icon>
              <GiftOutlined />
            </template>
            生成邀请码
          </a-button>
          <a-typography-text v-if="generatedCode" class="generated-code" code copyable>
            {{ generatedCode }}
          </a-typography-text>
          <a-input-search
            v-model:value="codeToCheck"
            placeholder="输入 12 位邀请码"
            enter-button="校验"
            allow-clear
            :loading="codeChecking"
            @search="handleCheckRegisterCode"
          />
          <a-tag v-if="codeCheckResult === true" color="success">可用</a-tag>
          <a-tag v-else-if="codeCheckResult === false" color="error">不可用</a-tag>
        </div>
      </section>

      <section class="table-panel">
        <div class="panel-heading">
          <div>
            <h2>用户列表</h2>
            <p>集中查看账号、联系方式、角色和创建时间。</p>
          </div>
          <a-tag color="cyan">{{ users.length }} 条</a-tag>
        </div>
        <a-table
          row-key="id"
          :columns="columns"
          :data-source="users"
          :loading="loading"
          :pagination="{ pageSize: 8, showSizeChanger: false, hideOnSinglePage: true }"
          :scroll="{ x: 1120 }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'profile'">
              <div class="user-cell">
                <a-avatar :src="resolveAvatarSrc(record.avatarUrl) || undefined" :size="36">
                  {{ getAvatarText(record) }}
                </a-avatar>
                <div>
                  <strong>{{ record.username || '未命名用户' }}</strong>
                  <span>{{ record.userAccount || '-' }}</span>
                </div>
              </div>
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

          <template #emptyText>
            <a-empty description="暂无用户数据" />
          </template>
        </a-table>
      </section>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { TableColumnsType } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import { DeleteOutlined, GiftOutlined, LoginOutlined, ReloadOutlined } from '@ant-design/icons-vue';

import { checkRegisterCode, deleteUser, generateRegisterCode, searchUsers } from '@/api/user';
import AppHeader from '@/components/AppHeader.vue';
import { useUserStore } from '@/stores/user';
import type { User } from '@/types/user';
import { resolveAvatarSrc } from '@/utils/avatar';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const codeGenerating = ref(false);
const codeChecking = ref(false);
const searchKeyword = ref('');
const users = ref<User[]>([]);
const generatedCode = ref('');
const codeToCheck = ref('');
const codeCheckResult = ref<boolean | null>(null);

const adminCount = computed(() => users.value.filter((user) => user.userRole === 1).length);
const normalUserCount = computed(() => users.value.length - adminCount.value);
const currentUserName = computed(
  () => userStore.currentUser?.username || userStore.currentUser?.userAccount || '请先登录',
);
const currentUserHint = computed(() =>
  userStore.currentUser ? '您正在使用当前账号。' : '登录后即可查看和管理用户。',
);

const columns: TableColumnsType<User> = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 76 },
  { title: '用户', key: 'profile', width: 220 },
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
    users.value = Array.isArray(result) ? result : [];
  } catch {
    users.value = [];
  } finally {
    loading.value = false;
  }
}

async function resetSearch() {
  searchKeyword.value = '';
  await fetchUsers();
}

async function handleDelete(id?: number) {
  if (!id) {
    message.error('用户 ID 无效');
    return;
  }

  try {
    const success = await deleteUser(id);
    if (!success) {
      message.error('删除失败，请稍后重试');
      return;
    }

    message.success('删除成功');
    await fetchUsers();
  } catch {
    // 统一错误提示由 request 拦截器处理。
  }
}

async function handleGenerateRegisterCode() {
  codeGenerating.value = true;
  try {
    const code = await generateRegisterCode();
    if (!code) {
      message.error('生成失败，请确认当前账号有管理权限');
      return;
    }

    generatedCode.value = code;
    codeToCheck.value = code;
    codeCheckResult.value = null;
    message.success('邀请码生成成功');
  } catch {
    // 统一错误提示由 request 拦截器处理。
  } finally {
    codeGenerating.value = false;
  }
}

async function handleCheckRegisterCode() {
  const code = codeToCheck.value.trim();
  codeCheckResult.value = null;
  if (!code) {
    message.warning('请先输入邀请码');
    return;
  }
  if (!/^[a-zA-Z0-9]{12}$/.test(code)) {
    message.warning('邀请码必须是 12 位大小写字母或数字');
    return;
  }

  codeChecking.value = true;
  try {
    const available = await checkRegisterCode(code);
    codeCheckResult.value = available;
    if (available) {
      message.success('邀请码可用');
      return;
    }

    message.error('邀请码无效或已被使用');
  } catch {
    // 统一错误提示由 request 拦截器处理。
  } finally {
    codeChecking.value = false;
  }
}

function goLogin() {
  void router.push({
    path: '/login',
    query: {
      redirect: '/users',
    },
  });
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
