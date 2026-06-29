export type ChapterAudioRequest = {
  languageCode?: string;
  voiceName?: string;
  engine?: string;
  speakingRate?: number;
  pitch?: number;
  audioEncoding?: 'MP3';
};

export type ChapterAudioStatus = 'PROCESSING' | 'READY' | 'FAILED';

export type ChapterAudioResponse = {
  assetId: number;
  chapterId: number;
  status: ChapterAudioStatus;
  audioUrl?: string | null;
  cached: boolean;
  provider: 'AWS_POLLY';
  languageCode: string;
  voiceName: string;
  engine: string;
  audioEncoding: string;
  fileSizeBytes?: number | null;
  durationMs?: number | null;
  errorMessage?: string | null;
  expiresAt?: string | null;
};

export type AudioVoice = {
  id: number;
  provider: 'AWS_POLLY';
  languageCode: string;
  voiceName: string;
  displayName: string;
  gender?: string | null;
  engine: string;
  enabled: boolean;
  defaultVoice: boolean;
  sortOrder: number;
};
