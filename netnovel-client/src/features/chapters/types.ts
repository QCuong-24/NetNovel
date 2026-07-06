import type { PageResponse } from '@/features/novels/types';

export type ChapterSummary = {
  chapterId: number;
  novelId: number;
  novelTitle: string;
  title: string;
  chapterNumber: number;
  updateAt?: string;
};

export type ChapterContent = ChapterSummary & {
  content: string;
};

export type ChapterPayload = {
  title: string;
  chapterNumber: number;
  content: string;
};

export type ChapterPage = PageResponse<ChapterSummary>;

export type ChapterPageParams = {
  page?: number;
  size?: number;
};
