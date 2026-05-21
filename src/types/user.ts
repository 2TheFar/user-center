export interface User {
  id?: number;
  username?: string | null;
  userAccount?: string | null;
  avatarUrl?: string | null;
  phone?: string | null;
  email?: string | null;
  gender?: number | null;
  userStatus?: number | null;
  createTime?: string | null;
  userRole?: number | null;
}

export interface LoginPayload {
  userAccount: string;
  userPassword: string;
}

export interface RegisterPayload {
  userAccount: string;
  userPassword: string;
  confirmPassword: string;
}
