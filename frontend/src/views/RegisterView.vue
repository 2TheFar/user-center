<template>
  <AuthShell>
    <a-card class="auth-card" :bordered="false">
      <div class="form-heading">
        <h2>注册</h2>
        <p>请输入账号信息和邀请码完成注册。</p>
      </div>

      <a-form :model="formState" :rules="rules" layout="vertical" @finish="handleRegister">
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
            autocomplete="new-password"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password
            v-model:value="formState.confirmPassword"
            placeholder="请再次输入密码"
            autocomplete="new-password"
          >
            <template #prefix>
              <SafetyCertificateOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item label="邀请码" name="registerCode">
          <a-input
            v-model:value="formState.registerCode"
            placeholder="请输入 12 位邀请码"
            allow-clear
            autocomplete="off"
          >
            <template #prefix>
              <KeyOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-button type="primary" html-type="submit" block size="large" :loading="loading">
          注册
        </a-button>
      </a-form>

      <div class="auth-footer">
        <span>已有账号？</span>
        <RouterLink to="/login">返回登录</RouterLink>
      </div>
    </a-card>
  </AuthShell>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { KeyOutlined, LockOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons-vue';
import { message, type FormProps } from 'ant-design-vue';

import AuthShell from '@/components/AuthShell.vue';
import { registerUser } from '@/api/user';
import type { RegisterPayload } from '@/types/user';

const router = useRouter();
const loading = ref(false);

const formState = reactive<RegisterPayload>({
  userAccount: '',
  userPassword: '',
  confirmPassword: '',
  registerCode: '',
});

const registerErrorText: Record<number, string> = {
  [-1]: '请完整填写注册信息',
  [-2]: '账号长度不能小于 6 位',
  [-3]: '密码长度不能小于 8 位',
  [-4]: '账号只能包含字母、数字和下划线',
  [-5]: '两次输入的密码不一致',
  [-6]: '账号已存在或保存失败',
  [-7]: '邀请码无效或已被使用',
  [-8]: '注册失败，请稍后重试',
};

const rules: FormProps['rules'] = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 6, message: '账号至少 6 位', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: '账号只能包含字母、数字和下划线',
      trigger: 'blur',
    },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { min: 8, message: '确认密码至少 8 位', trigger: 'blur' },
    {
      validator: async (_rule, value: string) => {
        if (value && value !== formState.userPassword) {
          return Promise.reject(new Error('两次输入的密码不一致'));
        }
        return Promise.resolve();
      },
      trigger: 'blur',
    },
  ],
  registerCode: [
    { required: true, message: '请输入邀请码', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9]{12}$/,
      message: '邀请码必须是 12 位大小写字母或数字',
      trigger: 'blur',
    },
  ],
};

async function handleRegister() {
  loading.value = true;
  try {
    const result = await registerUser({
      ...formState,
      registerCode: formState.registerCode.trim(),
    });
    if (typeof result === 'number' && result > 0) {
      message.success('注册成功，请登录');
      await router.push('/login');
      return;
    }

    message.error(registerErrorText[result ?? -1] || '注册失败，请稍后重试');
  } catch {
    // 统一错误提示由 request 拦截器处理。
  } finally {
    loading.value = false;
  }
}
</script>
