import request from './request';
import type { LoginPayload, RegisterPayload, User } from '@/types/user';

export function registerUser(payload: RegisterPayload) {
  return request.post<unknown, number | null>('/user/register', payload);
}

export function loginUser(payload: LoginPayload) {
  return request.post<unknown, User | null>('/user/login', payload);
}

export function logoutUser() {
  return request.post<unknown, boolean>('/user/logout');
}

export function searchUsers(username?: string) {
  return request.get<unknown, User[] | null>('/user/search', {
    params: {
      username: username || undefined,
    },
  });
}

export function deleteUser(id: number) {
  return request.post<unknown, boolean>('/user/delete', id, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
