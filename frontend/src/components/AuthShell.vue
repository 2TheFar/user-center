<template>
  <div class="auth-shell">
    <header class="auth-topbar">
      <button class="header-brand" type="button" @click="goUsers">
        <div class="brand-mark compact">UC</div>
        <span>用户中心</span>
      </button>
      <a-space>
        <a-button type="text" @click="goUsers">
          <template #icon>
            <TeamOutlined />
          </template>
          用户管理
        </a-button>
        <a-button v-if="route.path !== '/login'" type="text" @click="goLogin">登录</a-button>
        <a-button v-if="route.path !== '/register'" type="primary" @click="goRegister">注册</a-button>
      </a-space>
    </header>

    <main class="auth-page">
      <section class="auth-intro">
        <div class="brand-mark">UC</div>
        <h1>用户中心</h1>
        <p>轻量、清晰的用户登录与管理入口。</p>
      </section>
      <section class="auth-panel">
        <slot />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { TeamOutlined } from '@ant-design/icons-vue';

const route = useRoute();
const router = useRouter();

function goUsers() {
  void router.push('/users');
}

function goLogin() {
  void router.push('/login');
}

function goRegister() {
  void router.push('/register');
}
</script>
