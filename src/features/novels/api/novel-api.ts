import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { Novel, NovelInteraction, NovelListParams, NovelPayload, NovelSearchResult, PageResponse, Tag } from '../types';

function withPageParams(url: string, params: URLSearchParams) {
  return `${url}?${params.toString()}`;
}

function buildPageParams(page?: number, size?: number) {
  const params = new URLSearchParams();
  params.set('page', String(page ?? 0));
  params.set('size', String(size ?? 20));

  return params;
}

export async function getNovel(novelId: string) {
  const response = await httpClient.get<Novel>(endpoints.novels.detail(novelId));

  return response.data;
}

export async function getSimilarNovels(novelId: string, size = 5) {
  const pageParams = buildPageParams(0, size);
  const response = await httpClient.get<PageResponse<NovelSearchResult>>(
    withPageParams(endpoints.recommendations.similarNovels(novelId), pageParams),
  );

  return {
    ...response.data,
    content: response.data.content.map((result) => result.novel),
  } satisfies PageResponse<Novel>;
}

export async function getNovelList(params: NovelListParams) {
  const pageParams = buildPageParams(params.page, params.size);

  if (params.kind === 'newest') {
    const response = await httpClient.get<PageResponse<Novel>>(withPageParams(endpoints.novels.latest, pageParams));

    return response.data;
  }

  if (params.kind === 'completed') {
    const response = await httpClient.get<PageResponse<Novel>>(withPageParams(endpoints.novels.completed, pageParams));

    return response.data;
  }

  if (params.kind === 'hot' || params.kind === 'tag') {
    pageParams.set('sortMode', params.kind === 'hot' ? 'popular' : 'latest');
    if (params.kind === 'tag' && params.tagName) {
      pageParams.set('tag', params.tagName);
    }

    const response = await httpClient.get<PageResponse<NovelSearchResult>>(
      withPageParams(endpoints.search.novels, pageParams),
    );

    return {
      ...response.data,
      content: response.data.content.map((result) => result.novel),
    } satisfies PageResponse<Novel>;
  }

  const response = await httpClient.get<PageResponse<Novel>>(withPageParams(endpoints.novels.list, pageParams));

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

export async function deleteNovel(novelId: string) {
  await httpClient.delete(endpoints.novels.delete(novelId));
}

export async function increaseNovelView(novelId: string) {
  const response = await httpClient.post<NovelInteraction>(endpoints.novels.view(novelId));

  return response.data;
}

export async function getMyNovelInteraction(novelId: string) {
  const response = await httpClient.get<NovelInteraction>(endpoints.novels.myInteraction(novelId));

  return response.data;
}

export async function toggleNovelFollow(novelId: string) {
  const response = await httpClient.post<NovelInteraction>(endpoints.novels.toggleFollow(novelId));

  return response.data;
}

export async function toggleNovelLike(novelId: string) {
  const response = await httpClient.post<NovelInteraction>(endpoints.novels.toggleLike(novelId));

  return response.data;
}

export async function getTags() {
  const response = await httpClient.get<Tag[]>(endpoints.tags.list);

  return response.data;
}
