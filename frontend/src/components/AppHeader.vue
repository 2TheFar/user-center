<template>
  <a-layout-header class="app-header">
    <button class="header-brand" type="button" @click="goHome">
      <div class="brand-mark compact">UC</div>
      <span>用户中心</span>
    </button>

    <nav class="header-nav" aria-label="页面导航">
      <a-button v-if="userStore.isAdmin" :type="isUsersPage ? 'primary' : 'text'" @click="goUsers">
        <template #icon>
          <TeamOutlined />
        </template>
        用户管理
      </a-button>
      <a-button v-if="userStore.currentUser" :type="isProfilePage ? 'primary' : 'text'" @click="goProfile">
        <template #icon>
          <IdcardOutlined />
        </template>
        我的资料
      </a-button>
      <a-button v-if="showGuestLinks && !isLoginPage" type="text" @click="goLogin">
        <template #icon>
          <LoginOutlined />
        </template>
        登录
      </a-button>
      <a-button v-if="showGuestLinks && !isRegisterPage" type="text" @click="goRegister">
        <template #icon>
          <UserAddOutlined />
        </template>
        注册
      </a-button>
    </nav>

    <div class="header-actions">
      <a-tag v-if="userStore.currentUser" :color="userStore.isAdmin ? 'green' : 'blue'">
        {{ userStore.isAdmin ? '管理员' : '普通用户' }}
      </a-tag>
      <a-avatar v-if="userStore.currentUser" :src="userStore.currentUser.avatarUrl || undefined" :size="32">
        {{ userInitial }}
      </a-avatar>
      <span class="header-user">
        {{ displayName }}
      </span>
      <a-button v-if="userStore.currentUser" type="text" danger :loading="logoutLoading" @click="logout">
        <template #icon>
          <LogoutOutlined />
        </template>
        退出登录
      </a-button>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  IdcardOutlined,
  LoginOutlined,
  LogoutOutlined,
  TeamOutlined,
  UserAddOutlined,
} from '@ant-design/icons-vue';

import { logoutUser } from '@/api/user';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const logoutLoading = ref(false);

const isUsersPage = computed(() => route.path === '/users');
const isProfilePage = computed(() => route.path === '/profile' || route.path === '/');
const isLoginPage = computed(() => route.path === '/login');
const isRegisterPage = computed(() => route.path === '/register');
const showGuestLinks = computed(
  () => !userStore.currentUser?.id && !userStore.currentUser?.userAccount,
);
const displayName = computed(
  () => userStore.currentUser?.username || userStore.currentUser?.userAccount || '未登录',
);
const userInitial = computed(() => displayName.value.slice(0, 1).toUpperCase());

function goHome() {
  if (!userStore.currentUser) {
    void router.push('/login');
    return;
  }
  void router.push(userStore.isAdmin ? '/users' : '/profile');
}

function goUsers() {
  void router.push('/users');
}

function goProfile() {
  void router.push('/profile');
}

function goLogin() {
  void router.push('/login');
}

function goRegister() {
  void router.push('/register');
}

async function logout() {
  logoutLoading.value = true;
  try {
    const success = await logoutUser();
    if (!success) {
      message.error('退出登录失败，请稍后重试');
      return;
    }

    userStore.clearCurrentUser();
    message.success('退出登录成功');
    await router.push('/login');
  } catch {
    message.error('退出登录失败，请稍后重试');
  } finally {
    logoutLoading.value = false;
  }
}
</script>
