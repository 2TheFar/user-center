<template>
  <a-layout-header class="app-header">
    <button class="header-brand" type="button" @click="goUsers">
      <div class="brand-mark compact">UC</div>
      <span>用户中心</span>
    </button>

    <nav class="header-nav" aria-label="页面导航">
      <a-button :type="isUsersPage ? 'primary' : 'text'" @click="goUsers">
        <template #icon>
          <TeamOutlined />
        </template>
        用户管理
      </a-button>
      <a-button v-if="!isLoginPage" type="text" @click="goLogin">
        <template #icon>
          <LoginOutlined />
        </template>
        登录
      </a-button>
      <a-button v-if="!isRegisterPage" type="text" @click="goRegister">
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
      <span class="header-user">
        {{ userStore.currentUser?.userAccount || '未登录' }}
      </span>
      <a-button v-if="userStore.currentUser" type="text" danger @click="logout">
        <template #icon>
          <LogoutOutlined />
        </template>
        退出登录
      </a-button>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { LoginOutlined, LogoutOutlined, TeamOutlined, UserAddOutlined } from '@ant-design/icons-vue';

import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const isUsersPage = computed(() => route.path === '/users' || route.path === '/');
const isLoginPage = computed(() => route.path === '/login');
const isRegisterPage = computed(() => route.path === '/register');

function goUsers() {
  void router.push('/users');
}

function goLogin() {
  void router.push('/login');
}

function goRegister() {
  void router.push('/register');
}

function logout() {
  userStore.clearCurrentUser();
  message.success('已退出当前前端登录状态');
  void router.push('/login');
}
</script>
