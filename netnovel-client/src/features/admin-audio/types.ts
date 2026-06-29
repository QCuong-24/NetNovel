import type { PageResponse } from '@/features/novels/types';
import type { AudioVoice } from '@/features/audio/types';

export type AdminAudioDashboard = {
  totalAssets: number;
  readyAssets: number;
  processingAssets: number;
  failedAssets: number;
  totalStorageBytes: number;
  readyStorageBytes: number;
  generatedToday: number;
  providerCharactersToday: number;
  cacheHitCount: number;
  enabledVoices: number;
  totalVoices: number;
};

export type AdminAudioAssetStatus = 'PROCESSING' | 'READY' | 'FAILED' | 'CANCELLED' | 'EXPIRED';

export type AdminAudioAsset = {
  assetId: number;
  chapterId: number;
  novelId: number;
  chapterTitle: string;
  novelTitle: string;
  status: AdminAudioAssetStatus;
  provider: 'AWS_POLLY';
  languageCode: string;
  voiceName: string;
  engine: string;
  requestedByUserId?: number | null;
  sourceTextCharacters?: number | null;
  chunkCount?: number | null;
  providerRequestCount?: number | null;
  providerCharacterCount?: number | null;
  fileSizeBytes?: number | null;
  generationDurationMs?: number | null;
  cacheHitCount?: number | null;
  retryCount?: number | null;
  lastErrorCode?: string | null;
  errorMessage?: string | null;
  startedAt?: string | null;
  finishedAt?: string | null;
  lastAccessedAt?: string | null;
  expiresAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type AdminAudioAssetPage = PageResponse<AdminAudioAsset>;

export type AdminAudioVoice = AudioVoice;
