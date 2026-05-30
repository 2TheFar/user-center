<template>
  <AuthShell>
    <a-card class="auth-card" :bordered="false">
      <div class="form-heading">
        <h2>登录</h2>
        <p>欢迎回来，请登录后继续使用。</p>
      </div>

      <a-form :model="formState" :rules="rules" layout="vertical" @finish="handleLogin">
        <a-form-item label="账号" name="userAccount">
          <a-input
            v-model:value="formState.userAccount"
            placeholder="请输入账号"
            allow-clear
            autocomplete="username"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="密码" name="userPassword">
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="请输入密码"
            autocomplete="current-password"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-button type="primary" html-type="submit" block size="large" :loading="loading">
          登录
        </a-button>
      </a-form>

      <div class="auth-footer">
        <span>还没有账号？</span>
        <RouterLink to="/register">立即注册</RouterLink>
      </div>
    </a-card>
  </AuthShell>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue';
import { message, type FormProps } from 'ant-design-vue';

import AuthShell from '@/components/AuthShell.vue';
import { loginUser } from '@/api/user';
import { useUserStore } from '@/stores/user';
import type { LoginPayload } from '@/types/user';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const loading = ref(false);

const formState = reactive<LoginPayload>({
  userAccount: '',
  userPassword: '',
});

const rules: FormProps['rules'] = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function handleLogin() {
  loading.value = true;
  try {
    const user = await loginUser(formState);
    if (!user?.id) {
      message.error('账号或密码不正确');
      return;
    }

    userStore.setCurrentUser(user);
    message.success('登录成功');
    await router.push(typeof route.query.redirect === 'string' ? route.query.redirect : '/users');
  } catch {
    // 统一错误提示由 request 拦截器处理。
  } finally {
    loading.value = false;
  }
}
</script>
