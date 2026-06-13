import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type {
  AdvancedNovelSearchParams,
  ElasticReindexResponse,
  NovelSearchPage,
  NovelSearchResultPage,
  PublicNovelSearchParams,
} from '../types';

function buildSearchParams(params: Record<string, string | number | undefined>) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      searchParams.set(key, String(value));
    }
  });

  return searchParams;
}

function toNovelPage(page: NovelSearchResultPage): NovelSearchPage {
  return {
    ...page,
    content: page.content.map((result) => result.novel),
  };
}

export async function searchPublicNovels(params: PublicNovelSearchParams) {
  const searchParams = buildSearchParams({
    q: params.q,
    status: params.status,
    tag: params.tag,
    sortMode: params.sort ?? 'relevance',
    page: params.page ?? 0,
    size: params.size ?? 20,
  });
  const response = await httpClient.get<NovelSearchResultPage>(`${endpoints.search.novels}?${searchParams.toString()}`);

  return toNovelPage(response.data);
}

export async function searchAdvancedNovels(params: AdvancedNovelSearchParams) {
  const searchParams = buildSearchParams({
    q: params.q,
    status: params.status,
    tag: params.tag,
    source: params.source,
    crawled: params.crawled,
    page: params.page ?? 0,
    size: params.size ?? 20,
  });
  const response = await httpClient.get<NovelSearchResultPage>(
    `${endpoints.advancedSearch.novels}?${searchParams.toString()}`,
  );

  return toNovelPage(response.data);
}

export async function reindexNovels() {
  const response = await httpClient.post<ElasticReindexResponse>(endpoints.advancedSearch.reindexNovels);

  return response.data;
}
