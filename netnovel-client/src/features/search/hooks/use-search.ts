import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import {
  getElasticDiagnostics,
  getSearchSuggestions,
  rebuildNovelIndex,
  reindexNovels,
  searchAdvancedNovels,
  searchPublicNovels,
} from '../api/search-api';
import type { AdvancedNovelSearchParams, PublicNovelSearchParams } from '../types';

export function usePublicNovelSearch(params: PublicNovelSearchParams, enabled: boolean) {
  return useQuery({
    queryKey: [...queryKeys.search, 'public', params],
    queryFn: () => searchPublicNovels(params),
    enabled,
  });
}

export function useAdvancedNovelSearch(params: AdvancedNovelSearchParams, enabled: boolean) {
  return useQuery({
    queryKey: [...queryKeys.search, 'advanced', params],
    queryFn: () => searchAdvancedNovels(params),
    enabled,
  });
}

export function useSearchSuggestions(query: string, enabled = true) {
  const normalizedQuery = query.trim();

  return useQuery({
    queryKey: [...queryKeys.search, 'suggestions', normalizedQuery],
    queryFn: () => getSearchSuggestions(normalizedQuery),
    enabled: enabled && Boolean(normalizedQuery),
  });
}

export function useReindexNovelsMutation() {
  return useMutation({
    mutationFn: reindexNovels,
    onSuccess: () => {
      toast.success('Novel reindex started');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not reindex novels'));
    },
  });
}

export function useRebuildNovelIndexMutation() {
  return useMutation({
    mutationFn: rebuildNovelIndex,
    onSuccess: () => {
      toast.success('Novel index rebuilt');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not rebuild novel index'));
    },
  });
}

export function useElasticDiagnostics(enabled = true) {
  return useQuery({
    queryKey: [...queryKeys.search, 'elastic-diagnostics'],
    queryFn: getElasticDiagnostics,
    enabled,
  });
}
