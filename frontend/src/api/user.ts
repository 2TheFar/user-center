import request from './request';
import type { LoginPayload, RegisterPayload, UpdateProfilePayload, User } from '@/types/user';

export function registerUser(payload: RegisterPayload) {
  return request.post<unknown, number | null>('/user/register', payload);
}

export function generateRegisterCode() {
  return request.post<unknown, string | null>('/register-code/generate');
}

export function checkRegisterCode(code: string) {
  return request.get<unknown, boolean>('/register-code/check', {
    params: { code },
  });
}

export function loginUser(payload: LoginPayload) {
  return request.post<unknown, User | null>('/user/login', payload);
}

export function getCurrentUser() {
  return request.get<unknown, User | null>('/user/current');
}

export function logoutUser() {
  return request.post<unknown, boolean>('/user/logout');
}

export function updateCurrentUserProfile(payload: UpdateProfilePayload) {
  return request.post<unknown, User | null>('/user/profile/update', payload);
}

export function uploadCurrentUserAvatar(file: File) {
  const formData = new FormData();
  formData.append('avatar', file);
  return request.post<unknown, string | null>('/user/avatar/upload', formData);
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
