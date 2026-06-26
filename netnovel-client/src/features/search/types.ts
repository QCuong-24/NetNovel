import type { Novel, NovelSearchResult, PageResponse } from '@/features/novels/types';

export type SearchSort = 'relevance' | 'latest' | 'popular';

export type PublicNovelSearchParams = {
  q?: string;
  status?: string;
  genres?: string[];
  sort?: SearchSort;
  page?: number;
  size?: number;
};

export type AdvancedNovelSearchParams = {
  q?: string;
  status?: string;
  genres?: string[];
  tags?: string[];
  source?: string;
  crawled?: string;
  page?: number;
  size?: number;
};

export type NovelSearchPage = PageResponse<Novel>;

export type NovelSearchResultPage = PageResponse<NovelSearchResult>;

export type SearchSuggestionType = 'NOVEL' | 'AUTHOR' | 'GENRE';

export type SearchSuggestion = {
  type: SearchSuggestionType;
  id?: number | null;
  label: string;
};

export type ElasticReindexResponse = {
  indexedCount?: number;
  failedCount?: number;
  message?: string;
};
