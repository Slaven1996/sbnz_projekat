import { api } from './axios';
import type { LoginRequest, UserRequest, UserResponse, UserTokenState } from './types';

export async function login(body: LoginRequest): Promise<UserTokenState> {
  const { data } = await api.post<UserTokenState>('/auth/login', body);
  return data;
}

export async function register(body: UserRequest): Promise<UserResponse> {
  const { data } = await api.post<UserResponse>('/auth/register', body);
  return data;
}

export async function changePassword(body: {
  oldPassword: string;
  newPassword: string;
}): Promise<void> {
  await api.post('/auth/change-password', body);
}
