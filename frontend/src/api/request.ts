import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { message } from 'ant-design-vue';

interface BaseResponse<T> {
  code: number;
  data: T;
  message?: string;
  description?: string;
}

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true,
});

request.interceptors.request.use((config: InternalAxiosRequestConfig) => config);

function unwrapResponse(response: AxiosResponse): unknown {
  const result = response.data as BaseResponse<unknown>;
  if (typeof result?.code !== 'number') {
    return result;
  }

  if (result.code === 0) {
    return result.data;
  }

  const errorText = result.description || result.message || '请求处理失败';
  const isCurrentUserRequest = response.config.url?.includes('/user/current');
  if (result.code === 40100) {
    if (!isCurrentUserRequest) {
      message.warning('请先登录后再继续操作');
    }
  } else if (result.code === 40101) {
    message.warning('当前账号无权进行此操作');
  } else {
    message.error(errorText);
  }

  return Promise.reject(new Error(errorText));
}

request.interceptors.response.use(
  unwrapResponse as (response: AxiosResponse) => AxiosResponse,
  (error: AxiosError) => {
    if (error.response) {
      message.error('操作未完成，请稍后重试');
    } else {
      message.error('暂时无法连接服务，请稍后再试');
    }
    return Promise.reject(error);
  },
);

export default request;
