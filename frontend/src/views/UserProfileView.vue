<template>
  <a-layout class="app-layout">
    <AppHeader />

    <a-layout-content class="content-wrap profile-content">
      <section class="page-title-row">
        <div>
          <h1>我的资料</h1>
          <p>维护当前登录账号的基础资料，账号、角色和状态由系统统一管理。</p>
        </div>
        <a-space wrap>
          <a-button :loading="userStore.currentUserLoading" @click="refreshCurrentUser">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </section>

      <section class="current-user-panel profile-summary">
        <div class="current-user-main">
          <a-avatar :src="avatarPreview || undefined" :size="56">
            {{ userInitial }}
          </a-avatar>
          <div>
            <h2>{{ displayName }}</h2>
            <p>{{ accountText }}</p>
          </div>
        </div>
        <a-descriptions size="small" :column="{ xs: 1, sm: 2, md: 3 }">
          <a-descriptions-item label="用户 ID">
            {{ userStore.currentUser?.id || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="账号">
            {{ userStore.currentUser?.userAccount || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="角色">
            <a-tag :color="userStore.isAdmin ? 'green' : 'blue'">
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </section>

      <section class="workspace-panel profile-form-panel">
        <div class="panel-heading">
          <div>
            <h2>修改信息</h2>
            <p>保存后会同步更新页面顶部展示的昵称和头像。</p>
          </div>
        </div>

        <a-form
          class="profile-form"
          :model="formState"
          :rules="rules"
          :label-col="{ xs: { span: 24 }, sm: { span: 4 } }"
          :wrapper-col="{ xs: { span: 24 }, sm: { span: 18 } }"
          @finish="handleSubmit"
        >
          <a-form-item label="昵称" name="username">
            <a-input v-model:value="formState.username" placeholder="请输入昵称" allow-clear>
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="头像" name="avatarUrl">
            <a-input v-model:value="formState.avatarUrl" placeholder="请输入头像图片 URL" allow-clear>
              <template #prefix>
                <LinkOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="性别" name="gender">
            <a-radio-group v-model:value="formState.gender">
              <a-radio :value="0">女</a-radio>
              <a-radio :value="1">男</a-radio>
              <a-radio :value="null">保密</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item label="手机" name="phone">
            <a-input v-model:value="formState.phone" placeholder="请输入手机号" allow-clear>
              <template #prefix>
                <PhoneOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="邮箱" name="email">
            <a-input v-model:value="formState.email" placeholder="请输入邮箱" allow-clear>
              <template #prefix>
                <MailOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item :wrapper-col="{ xs: { span: 24 }, sm: { offset: 4, span: 18 } }">
            <a-space wrap>
              <a-button type="primary" html-type="submit" :loading="saving">
                <template #icon>
                  <SaveOutlined />
                </template>
                保存资料
              </a-button>
              <a-button @click="resetForm">恢复当前资料</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </section>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import type { FormProps } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import {
  LinkOutlined,
  MailOutlined,
  PhoneOutlined,
  ReloadOutlined,
  SaveOutlined,
  UserOutlined,
} from '@ant-design/icons-vue';

import { updateCurrentUserProfile } from '@/api/user';
import AppHeader from '@/components/AppHeader.vue';
import { useUserStore } from '@/stores/user';
import type { UpdateProfilePayload, User } from '@/types/user';

const router = useRouter();
const userStore = useUserStore();
const saving = ref(false);

const formState = reactive<UpdateProfilePayload>({
  username: '',
  avatarUrl: '',
  phone: '',
  email: '',
  gender: null,
});

const rules: FormProps['rules'] = {
  username: [{ max: 256, message: '昵称不能超过 256 个字符', trigger: 'blur' }],
  avatarUrl: [
    { max: 1024, message: '头像地址不能超过 1024 个字符', trigger: 'blur' },
    {
      pattern: /^$|^https?:\/\//,
      message: '头像地址必须以 http:// 或 https:// 开头',
      trigger: 'blur',
    },
  ],
  phone: [
    {
      pattern: /^$|^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur',
    },
  ],
  email: [
    { max: 256, message: '邮箱不能超过 256 个字符', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入正确的邮箱',
      trigger: 'blur',
    },
  ],
};

const displayName = computed(
  () => formState.username?.trim() || userStore.currentUser?.userAccount || '未命名用户',
);
const accountText = computed(() => `账号：${userStore.currentUser?.userAccount || '-'}`);
const avatarPreview = computed(() => formState.avatarUrl?.trim() || userStore.currentUser?.avatarUrl || '');
const userInitial = computed(() => displayName.value.slice(0, 1).toUpperCase());

onMounted(async () => {
  if (!userStore.currentUser) {
    const user = await userStore.fetchCurrentUser();
    if (!user) {
      await router.replace({
        path: '/login',
        query: { redirect: '/profile' },
      });
      return;
    }
  }
  fillForm(userStore.currentUser);
});

watch(
  () => userStore.currentUser,
  (user) => {
    fillForm(user);
  },
);

async function refreshCurrentUser() {
  const user = await userStore.fetchCurrentUser();
  if (!user) {
    await router.replace({
      path: '/login',
      query: { redirect: '/profile' },
    });
  }
}

async function handleSubmit() {
  saving.value = true;
  try {
    const updatedUser = await updateCurrentUserProfile(buildPayload());
    if (!updatedUser?.id) {
      message.error('保存失败，请稍后重试');
      return;
    }

    userStore.setCurrentUser(updatedUser);
    message.success('资料保存成功');
  } catch {
    // 统一错误提示由 request 拦截器处理。
  } finally {
    saving.value = false;
  }
}

function resetForm() {
  fillForm(userStore.currentUser);
}

function fillForm(user: User | null) {
  formState.username = user?.username || '';
  formState.avatarUrl = user?.avatarUrl || '';
  formState.phone = user?.phone || '';
  formState.email = user?.email || '';
  formState.gender = user?.gender ?? null;
}

function buildPayload(): UpdateProfilePayload {
  return {
    username: normalizeValue(formState.username),
    avatarUrl: normalizeValue(formState.avatarUrl),
    phone: normalizeValue(formState.phone),
    email: normalizeValue(formState.email),
    gender: formState.gender ?? null,
  };
}

function normalizeValue(value?: string | null) {
  const normalizedValue = value?.trim();
  return normalizedValue || null;
}
</script>
