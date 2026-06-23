import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { UserEventDataReport, UserNovelInteractionPage, UserNovelInteractionRebuild } from '../types';

export async function getUserEventReport(days: number) {
  const response = await httpClient.get<UserEventDataReport>(`${endpoints.dataReports.userEvents}?days=${days}`);

  return response.data;
}

export async function rebuildUserNovelInteractions() {
  const response = await httpClient.post<UserNovelInteractionRebuild>(endpoints.dataReports.rebuildInteractions);

  return response.data;
}

export async function getUserNovelInteractions({ page = 0, size = 10, userId }: { page?: number; size?: number; userId?: number }) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (userId) {
    params.set('userId', String(userId));
  }

  const response = await httpClient.get<UserNovelInteractionPage>(`${endpoints.dataReports.userNovelInteractions}?${params.toString()}`);

  return response.data;
}
