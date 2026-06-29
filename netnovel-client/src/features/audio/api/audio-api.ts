import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { AudioVoice, ChapterAudioRequest, ChapterAudioResponse } from '../types';

export async function createChapterAudio(chapterId: string, payload?: ChapterAudioRequest) {
  const response = await httpClient.post<ChapterAudioResponse>(endpoints.chapters.audio(chapterId), payload ?? {});

  return response.data;
}

export async function getChapterAudioAsset(assetId: string) {
  const response = await httpClient.get<ChapterAudioResponse>(endpoints.audioAssets.detail(assetId));

  return response.data;
}

export async function getAudioVoices(languageCode?: string) {
  const searchParams = new URLSearchParams();
  if (languageCode) {
    searchParams.set('languageCode', languageCode);
  }

  const query = searchParams.toString();
  const response = await httpClient.get<AudioVoice[]>(query ? `${endpoints.audio.voices}?${query}` : endpoints.audio.voices);

  return response.data;
}
