import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { AuthResponse, GoogleLoginRequest, LoginRequest, RegisterRequest, User } from '../types';

export async function login(payload: LoginRequest) {
  const response = await httpClient.post<AuthResponse>(endpoints.auth.login, payload);

  return response.data;
}

export async function register(payload: RegisterRequest) {
  const response = await httpClient.post<AuthResponse>(endpoints.auth.register, payload);

  return response.data;
}

export async function loginWithGoogle(payload: GoogleLoginRequest) {
  const response = await httpClient.post<AuthResponse>(endpoints.auth.google, payload);

  return response.data;
}

export async function getCurrentUser() {
  const response = await httpClient.get<User>(endpoints.auth.me);

  return response.data;
}

export async function logout(refreshToken: string) {
  await httpClient.post(endpoints.auth.logout, { refreshToken });
}
