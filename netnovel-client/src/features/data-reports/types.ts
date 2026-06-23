import type { PageResponse } from '@/features/novels/types';

export type UserEventDataReport = {
  periodDays: number;
  from: string;
  to: string;
  totalEvents: number;
  eventsByType: Record<string, number>;
  activeUsers: number;
  interactedNovels: number;
  usersWithAtLeast3DistinctNovels: number;
  usersWithAtLeast5DistinctNovels: number;
  novelsWithAtLeast3Users: number;
  novelsWithAtLeast5Users: number;
  averageDistinctNovelsPerActiveUser: number;
  averageUsersPerInteractedNovel: number;
};

export type UserNovelInteraction = {
  userId: number;
  novelId: number;
  viewNovelCount: number;
  viewChapterCount: number;
  commentCount: number;
  replyCount: number;
  followed: boolean;
  liked: boolean;
  bookmarked: boolean;
  interactionScore: number;
  firstInteractedAt?: string | null;
  lastInteractedAt?: string | null;
  calculatedAt?: string | null;
};

export type UserNovelInteractionPage = PageResponse<UserNovelInteraction>;

export type UserNovelInteractionRebuild = {
  interactionCount: number;
  rebuiltAt: string;
};
