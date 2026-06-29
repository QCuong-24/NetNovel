import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { AdminAudioAssetPage, AdminAudioAssetStatus, AdminAudioDashboard, AdminAudioVoice } from '../types';

type AdminAudioAssetListParams = {
  page?: number;
  size?: number;
  status?: AdminAudioAssetStatus | '';
};

function withAssetParams(params: AdminAudioAssetListParams) {
  const searchParams = new URLSearchParams();
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 10));
  if (params.status) {
    searchParams.set('status', params.status);
  }

  return searchParams.toString();
}

export async function getAdminAudioDashboard() {
  const response = await httpClient.get<AdminAudioDashboard>(endpoints.adminAudio.dashboard);

  return response.data;
}

export async function getAdminAudioAssets(params: AdminAudioAssetListParams = {}) {
  const response = await httpClient.get<AdminAudioAssetPage>(`${endpoints.adminAudio.assets}?${withAssetParams(params)}`);

  return response.data;
}

export async function retryAdminAudioAsset(assetId: string) {
  await httpClient.post(endpoints.adminAudio.assetRetry(assetId));
}

export async function deleteAdminAudioAsset(assetId: string) {
  await httpClient.delete(endpoints.adminAudio.asset(assetId));
}

export async function cleanupExpiredAudioAssets() {
  const response = await httpClient.post<{ deleted: number }>(endpoints.adminAudio.cleanupExpired);

  return response.data;
}

export async function getAdminAudioVoices() {
  const response = await httpClient.get<AdminAudioVoice[]>(endpoints.adminAudio.voices);

  return response.data;
}

export async function syncAdminAudioVoices() {
  const response = await httpClient.post<AdminAudioVoice[]>(endpoints.adminAudio.syncVoices);

  return response.data;
}

export async function updateAdminAudioVoice(voiceId: string, payload: Partial<Pick<AdminAudioVoice, 'enabled' | 'defaultVoice' | 'sortOrder'>>) {
  const response = await httpClient.patch<AdminAudioVoice>(endpoints.adminAudio.voice(voiceId), payload);

  return response.data;
}
