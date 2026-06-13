import type { Novel, NovelSearchResult, PageResponse } from '@/features/novels/types';

export type SearchSort = 'relevance' | 'latest' | 'popular';

export type PublicNovelSearchParams = {
  q?: string;
  status?: string;
  tag?: string;
  sort?: SearchSort;
  page?: number;
  size?: number;
};

export type AdvancedNovelSearchParams = {
  q?: string;
  status?: string;
  tag?: string;
  source?: string;
  crawled?: string;
  page?: number;
  size?: number;
};

export type NovelSearchPage = PageResponse<Novel>;

export type NovelSearchResultPage = PageResponse<NovelSearchResult>;

export type ElasticReindexResponse = {
  indexedCount?: number;
  failedCount?: number;
  message?: string;
};
