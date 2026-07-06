import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { ChapterContent, ChapterPage, ChapterPageParams, ChapterPayload, ChapterSummary } from '../types';

function withPageParams(url: string, params: ChapterPageParams = {}) {
  const searchParams = new URLSearchParams();
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 10));

  return `${url}?${searchParams.toString()}`;
}

export async function getChapter(chapterId: string) {
  const response = await httpClient.get<ChapterContent>(endpoints.chapters.detail(chapterId));

  return response.data;
}

export async function getNovelChapters(novelId: string) {
  const response = await httpClient.get<ChapterSummary[]>(`${endpoints.chapters.byNovel(novelId)}/all`);

  return response.data;
}

export async function getNovelChapterPage(novelId: string, params: ChapterPageParams = {}) {
  const response = await httpClient.get<ChapterPage>(withPageParams(endpoints.chapters.byNovel(novelId), params));

  return response.data;
}

export async function createChapter(novelId: string, payload: ChapterPayload) {
  const response = await httpClient.post<ChapterContent>(endpoints.chapters.byNovel(novelId), payload);

  return response.data;
}

export async function updateChapter(chapterId: string, payload: ChapterPayload) {
  const response = await httpClient.put<ChapterContent>(endpoints.chapters.update(chapterId), payload);

  return response.data;
}

export async function deleteChapter(chapterId: string) {
  await httpClient.delete(endpoints.chapters.delete(chapterId));
}
