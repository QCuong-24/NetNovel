import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { Bookmark, BookmarkKind, BookmarkPage, FollowedNovelPage, LastReadNovelPage } from '../types';

type CollectionPageParams = {
  page?: number;
  size?: number;
};

function withPageParams(endpoint: string, params: CollectionPageParams) {
  const searchParams = new URLSearchParams();
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 6));

  return `${endpoint}?${searchParams.toString()}`;
}

export async function getLastReading(params: CollectionPageParams = {}) {
  const response = await httpClient.get<LastReadNovelPage>(withPageParams(endpoints.lastReads.list, params));

  return response.data;
}

export async function getBookmarks(kind: BookmarkKind, params: CollectionPageParams = {}) {
  const endpoint = kind === 'novels' ? endpoints.bookmarks.novels : endpoints.bookmarks.chapters;
  const response = await httpClient.get<BookmarkPage>(withPageParams(endpoint, params));

  return response.data;
}

export async function getFollowedNovels(params: CollectionPageParams = {}) {
  const response = await httpClient.get<FollowedNovelPage>(withPageParams(endpoints.follows.novels, params));

  return response.data;
}

export async function updateLastRead(novelId: string, chapterId: string) {
  const response = await httpClient.put(endpoints.lastReads.updateNovelChapter(novelId, chapterId));

  return response.data;
}

export async function getNovelBookmarkStatus(novelId: string) {
  const response = await httpClient.get<{ bookmarked: boolean }>(endpoints.bookmarks.novelExists(novelId));

  return response.data.bookmarked;
}

export async function getChapterBookmarkStatus(chapterId: string) {
  const response = await httpClient.get<{ bookmarked: boolean }>(endpoints.bookmarks.chapterExists(chapterId));

  return response.data.bookmarked;
}

export async function createNovelBookmark(novelId: string) {
  const response = await httpClient.post<Bookmark>(endpoints.bookmarks.createNovel(novelId));

  return response.data;
}

export async function createChapterBookmark(chapterId: string) {
  const response = await httpClient.post<Bookmark>(endpoints.bookmarks.createChapter(chapterId));

  return response.data;
}

export async function deleteNovelBookmark(novelId: string) {
  await httpClient.delete(endpoints.bookmarks.deleteNovel(novelId));
}

export async function deleteChapterBookmark(chapterId: string) {
  await httpClient.delete(endpoints.bookmarks.deleteChapter(chapterId));
}
