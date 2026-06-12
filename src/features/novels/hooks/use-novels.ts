import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { createNovel, getNovel, getTags, updateNovel } from '../api/novel-api';
import type { NovelPayload } from '../types';

export function useNovel(novelId?: string) {
  return useQuery({
    queryKey: [...queryKeys.novels, novelId],
    queryFn: () => getNovel(novelId!),
    enabled: Boolean(novelId),
  });
}

export function useTags() {
  return useQuery({
    queryKey: queryKeys.tags,
    queryFn: getTags,
    staleTime: 5 * 60_000,
  });
}

export function useCreateNovelMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createNovel,
    onSuccess: (novel) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.novels });
      toast.success(`Created "${novel.title}"`);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not create novel'));
    },
  });
}

export function useUpdateNovelMutation(novelId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: NovelPayload) => updateNovel(novelId, payload),
    onSuccess: (novel) => {
      queryClient.setQueryData([...queryKeys.novels, novelId], novel);
      queryClient.invalidateQueries({ queryKey: queryKeys.novels });
      toast.success(`Updated "${novel.title}"`);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update novel'));
    },
  });
}
