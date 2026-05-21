import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'ant-design-vue';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  withCredentials: true,
});

request.interceptors.request.use((config: InternalAxiosRequestConfig) => config);

request.interceptors.response.use(
  (response) => response.data,
  (error: AxiosError) => {
    if (error.response) {
      message.error(`请求失败：${error.response.status}`);
    } else {
      message.error('无法连接后端服务，请确认 Spring Boot 已启动');
    }
    return Promise.reject(error);
  },
);

export default request;
