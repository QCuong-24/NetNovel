import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { User } from '@/features/auth/types';
import type { AdminUserPage, AdminUserPayload } from '../types';

type AdminUserListParams = {
  page?: number;
  size?: number;
};

function withPageParams(params: AdminUserListParams) {
  const searchParams = new URLSearchParams();
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 10));

  return searchParams.toString();
}

export async function getAdminUsers(params: AdminUserListParams = {}) {
  const response = await httpClient.get<AdminUserPage>(`${endpoints.adminUsers.list}?${withPageParams(params)}`);

  return response.data;
}

export async function getAdminUser(userId: string) {
  const response = await httpClient.get<User>(endpoints.adminUsers.detail(userId));

  return response.data;
}

export async function createAdminUser(payload: AdminUserPayload) {
  const response = await httpClient.post<User>(endpoints.adminUsers.create, payload);

  return response.data;
}

export async function updateAdminUser(userId: string, payload: AdminUserPayload) {
  const response = await httpClient.put<User>(endpoints.adminUsers.update(userId), payload);

  return response.data;
}

export async function deleteAdminUser(userId: string) {
  await httpClient.delete(endpoints.adminUsers.delete(userId));
}
