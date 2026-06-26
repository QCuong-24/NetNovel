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
  indexName?: string;
  indexed?: number;
  failed?: number;
  indexedCount?: number;
  failedCount?: number;
  message?: string;
};

export type SemanticNovelSearchParams = {
  q?: string;
  page?: number;
  size?: number;
};

export type ElasticDiagnosticsBucket = {
  key: string;
  count: number;
};

export type ElasticDiagnosticsResponse = {
  enabled: boolean;
  indexName: string;
  exists: boolean;
  documentCount: number;
  embeddingDocumentCount: number;
  mappingVersion: string;
  fieldMappings: Record<string, string>;
  statusBuckets: ElasticDiagnosticsBucket[];
  topGenres: ElasticDiagnosticsBucket[];
  topTags: ElasticDiagnosticsBucket[];
  crawledBuckets: ElasticDiagnosticsBucket[];
  embeddingModelBuckets: ElasticDiagnosticsBucket[];
};
