import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { createChapterAudio, getAudioVoices, getChapterAudioAsset } from '../api/audio-api';
import type { ChapterAudioRequest } from '../types';

export function useChapterAudioAsset(assetId?: string, enabled = true) {
  return useQuery({
    queryKey: [...queryKeys.audio, 'asset', assetId],
    queryFn: () => getChapterAudioAsset(assetId!),
    enabled: Boolean(assetId) && enabled,
    refetchInterval: (query) => (query.state.data?.status === 'PROCESSING' ? 2500 : false),
  });
}

export function useAudioVoices(languageCode?: string) {
  return useQuery({
    queryKey: [...queryKeys.audio, 'voices', languageCode],
    queryFn: () => getAudioVoices(languageCode),
  });
}

export function useCreateChapterAudioMutation(chapterId?: string) {
  return useMutation({
    mutationFn: (payload?: ChapterAudioRequest) => createChapterAudio(chapterId!, payload),
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not generate chapter audio'));
    },
  });
}
