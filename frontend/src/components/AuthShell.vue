<template>
  <div class="auth-shell">
    <header class="auth-topbar">
      <button class="header-brand" type="button" @click="goUsers">
        <div class="brand-mark compact">UC</div>
        <span>用户中心</span>
      </button>
      <a-space class="auth-nav">
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
        <a-tag color="cyan">账号管理平台</a-tag>
        <h1>用户中心</h1>
        <p>统一管理账号信息，快速完成登录、注册与用户维护。</p>
        <div class="auth-metrics" aria-label="核心能力">
          <div>
            <strong>安全</strong>
            <span>账号登录</span>
          </div>
          <div>
            <strong>清晰</strong>
            <span>用户管理</span>
          </div>
          <div>
            <strong>便捷</strong>
            <span>邀请注册</span>
          </div>
        </div>
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
