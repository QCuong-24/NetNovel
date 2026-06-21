import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { UserProfile } from '../types';

export async function getUserProfile(userId: string) {
  const response = await httpClient.get<UserProfile>(endpoints.users.detail(userId));

  return response.data;
}
