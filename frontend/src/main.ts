import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

import App from './App.vue';
import router from './router';
import { pinia } from './stores';
import { useUserStore } from './stores/user';
import './styles/main.css';

const app = createApp(App);

app.use(pinia);
app.use(router);
app.use(Antd);

const userStore = useUserStore();

void userStore.fetchCurrentUser().finally(() => {
  app.mount('#app');
});
