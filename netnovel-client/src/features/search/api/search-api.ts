import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type {
  AdvancedNovelSearchParams,
  ElasticDiagnosticsResponse,
  ElasticReindexResponse,
  NovelSearchPage,
  NovelSearchResultPage,
  PublicNovelSearchParams,
  SearchSuggestion,
} from '../types';

function buildSearchParams(params: Record<string, string | number | string[] | undefined>) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.filter(Boolean).forEach((item) => searchParams.append(key, item));
    } else if (value !== undefined && value !== '') {
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
    genre: params.genres,
    sortMode: params.sort ?? 'relevance',
    page: params.page ?? 0,
    size: params.size ?? 18,
  });
  const response = await httpClient.get<NovelSearchResultPage>(`${endpoints.search.novels}?${searchParams.toString()}`);

  return toNovelPage(response.data);
}

export async function searchAdvancedNovels(params: AdvancedNovelSearchParams) {
  const searchParams = buildSearchParams({
    q: params.q,
    status: params.status,
    genre: params.genres,
    tag: params.tags,
    source: params.source,
    crawled: params.crawled,
    page: params.page ?? 0,
    size: params.size ?? 18,
  });
  const response = await httpClient.get<NovelSearchResultPage>(
    `${endpoints.advancedSearch.novels}?${searchParams.toString()}`,
  );

  return toNovelPage(response.data);
}

export async function getSearchSuggestions(query: string, limit = 8) {
  const searchParams = buildSearchParams({
    q: query,
    limit,
  });
  const response = await httpClient.get<SearchSuggestion[]>(`${endpoints.search.suggestions}?${searchParams.toString()}`);

  return response.data;
}

export async function reindexNovels() {
  const response = await httpClient.post<ElasticReindexResponse>(endpoints.advancedSearch.reindexNovels);

  return response.data;
}

export async function rebuildNovelIndex() {
  const response = await httpClient.post<ElasticReindexResponse>(endpoints.advancedSearch.rebuildNovels);

  return response.data;
}

export async function getElasticDiagnostics() {
  const response = await httpClient.get<ElasticDiagnosticsResponse>(endpoints.advancedSearch.diagnostics);

  return response.data;
}
