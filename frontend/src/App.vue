<template>
  <a-config-provider :locale="zhCN" :theme="theme">
    <a-app>
      <RouterView />
    </a-app>
  </a-config-provider>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context';

const theme: ThemeConfig = {
  token: {
    colorPrimary: '#0891b2',
    colorSuccess: '#16a34a',
    colorInfo: '#0891b2',
    borderRadius: 6,
    colorBgLayout: '#f6f8fb',
    colorText: '#172033',
    fontFamily:
      '-apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif',
  },
};
</script>
