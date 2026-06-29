import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import {
  cleanupExpiredAudioAssets,
  deleteAdminAudioAsset,
  getAdminAudioAssets,
  getAdminAudioDashboard,
  getAdminAudioVoices,
  retryAdminAudioAsset,
  syncAdminAudioVoices,
  updateAdminAudioVoice,
} from '../api/admin-audio-api';
import type { AdminAudioAssetStatus, AdminAudioVoice } from '../types';

type AssetParams = {
  page?: number;
  size?: number;
  status?: AdminAudioAssetStatus | '';
};

export function useAdminAudioDashboard() {
  return useQuery({
    queryKey: [...queryKeys.adminAudio, 'dashboard'],
    queryFn: getAdminAudioDashboard,
  });
}

export function useAdminAudioAssets(params: AssetParams) {
  return useQuery({
    queryKey: [...queryKeys.adminAudio, 'assets', params],
    queryFn: () => getAdminAudioAssets(params),
  });
}

export function useAdminAudioVoices() {
  return useQuery({
    queryKey: [...queryKeys.adminAudio, 'voices'],
    queryFn: getAdminAudioVoices,
  });
}

export function useRetryAdminAudioAssetMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: retryAdminAudioAsset,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudio });
      toast.success('Audio retry started');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not retry audio')),
  });
}

export function useDeleteAdminAudioAssetMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteAdminAudioAsset,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudio });
      toast.success('Audio asset deleted');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not delete audio asset')),
  });
}

export function useCleanupExpiredAudioMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: cleanupExpiredAudioAssets,
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudio });
      toast.success(`Deleted ${result.deleted} expired audio assets`);
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not cleanup audio assets')),
  });
}

export function useSyncAdminAudioVoicesMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: syncAdminAudioVoices,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudio });
      queryClient.invalidateQueries({ queryKey: queryKeys.audio });
      toast.success('Audio voices synced');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not sync audio voices')),
  });
}

export function useUpdateAdminAudioVoiceMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ voiceId, payload }: { voiceId: string; payload: Partial<Pick<AdminAudioVoice, 'enabled' | 'defaultVoice' | 'sortOrder'>> }) =>
      updateAdminAudioVoice(voiceId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudio });
      queryClient.invalidateQueries({ queryKey: queryKeys.audio });
      toast.success('Audio voice updated');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not update audio voice')),
  });
}

export function useKeepOnlyDefaultAdminAudioVoiceMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (voices: AdminAudioVoice[]) => {
      const defaultVoice = voices.find((voice) => voice.defaultVoice) ?? voices[0];
      if (!defaultVoice) {
        return;
      }

      await Promise.all(
        voices.map((voice) =>
          updateAdminAudioVoice(String(voice.id), {
            enabled: voice.id === defaultVoice.id,
            defaultVoice: voice.id === defaultVoice.id,
          })
        )
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminAudio });
      queryClient.invalidateQueries({ queryKey: queryKeys.audio });
      toast.success('Only default voice is enabled');
    },
    onError: (error) => toast.error(getApiErrorMessage(error, 'Could not update audio voices')),
  });
}
