import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { Novel, NovelPayload, Tag } from '../types';

export async function getNovel(novelId: string) {
  const response = await httpClient.get<Novel>(endpoints.novels.detail(novelId));

  return response.data;
}

export async function createNovel(payload: NovelPayload) {
  const response = await httpClient.post<Novel>(endpoints.novels.create, payload);

  return response.data;
}

export async function updateNovel(novelId: string, payload: NovelPayload) {
  const response = await httpClient.put<Novel>(endpoints.novels.update(novelId), payload);

  return response.data;
}

export async function getTags() {
  const response = await httpClient.get<Tag[]>(endpoints.tags.list);

  return response.data;
}
