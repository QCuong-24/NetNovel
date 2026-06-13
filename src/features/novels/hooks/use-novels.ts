import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import {
  createNovel,
  deleteNovel,
  getMyNovelInteraction,
  getNovel,
  getNovelList,
  getSimilarNovels,
  getTags,
  increaseNovelView,
  toggleNovelFollow,
  toggleNovelLike,
  updateNovel,
} from '../api/novel-api';
import { hasAuthTokens } from '@/features/auth/lib/auth-storage';
import type { Novel, NovelInteraction, NovelListParams, NovelPayload } from '../types';

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

export function useNovelList(params: NovelListParams) {
  return useQuery({
    queryKey: [...queryKeys.novels, 'list', params],
    queryFn: () => getNovelList(params),
  });
}

export function useSimilarNovels(novelId?: string) {
  return useQuery({
    queryKey: [...queryKeys.novels, novelId, 'similar'],
    queryFn: () => getSimilarNovels(novelId!),
    enabled: Boolean(novelId),
  });
}

function mergeNovelInteraction(novel: Novel | undefined, interaction: NovelInteraction) {
  if (!novel) {
    return novel;
  }

  return {
    ...novel,
    views: interaction.views,
    follows: interaction.follows,
    likes: interaction.likes,
  };
}

export function useMyNovelInteraction(novelId?: string) {
  return useQuery({
    queryKey: [...queryKeys.novels, novelId, 'interaction'],
    queryFn: () => getMyNovelInteraction(novelId!),
    enabled: Boolean(novelId && hasAuthTokens()),
  });
}

export function useIncreaseNovelViewMutation(novelId?: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => increaseNovelView(novelId!),
    onSuccess: (interaction) => {
      queryClient.setQueryData<Novel | undefined>([...queryKeys.novels, novelId], (novel) =>
        mergeNovelInteraction(novel, interaction),
      );
      queryClient.setQueryData([...queryKeys.novels, novelId, 'interaction'], interaction);
    },
  });
}

export function useToggleNovelFollowMutation(novelId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => toggleNovelFollow(novelId),
    onSuccess: (interaction) => {
      queryClient.setQueryData<Novel | undefined>([...queryKeys.novels, novelId], (novel) =>
        mergeNovelInteraction(novel, interaction),
      );
      queryClient.setQueryData([...queryKeys.novels, novelId, 'interaction'], interaction);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update follow'));
    },
  });
}

export function useToggleNovelLikeMutation(novelId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => toggleNovelLike(novelId),
    onSuccess: (interaction) => {
      queryClient.setQueryData<Novel | undefined>([...queryKeys.novels, novelId], (novel) =>
        mergeNovelInteraction(novel, interaction),
      );
      queryClient.setQueryData([...queryKeys.novels, novelId, 'interaction'], interaction);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update like'));
    },
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

export function useDeleteNovelMutation(novelId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => deleteNovel(novelId),
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: [...queryKeys.novels, novelId] });
      queryClient.invalidateQueries({ queryKey: queryKeys.novels });
      toast.success('Novel deleted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not delete novel'));
    },
  });
}
