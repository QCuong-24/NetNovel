import type { Novel, PageResponse } from '@/features/novels/types';

export type BookmarkKind = 'novels' | 'chapters';

export type LastReadNovel = {
  lastReadId: number;
  userId: number;
  novelId?: number | null;
  novelTitle?: string | null;
  author?: string | null;
  coverImageUrl?: string | null;
  chapterId?: number | null;
  chapterTitle?: string | null;
  chapterNumber?: number | null;
  lastReadAt?: string | null;
};

export type Bookmark = {
  bookmarkId: number;
  userId: number;
  novelId?: number | null;
  novelTitle?: string | null;
  author?: string | null;
  coverImageUrl?: string | null;
  chapterId?: number | null;
  chapterTitle?: string | null;
  chapterNumber?: number | null;
  createdAt?: string | null;
};

export type FollowedNovel = {
  followId: number;
  userId: number;
  novel: Novel;
  followedAt?: string | null;
};

export type LastReadNovelPage = PageResponse<LastReadNovel>;

export type BookmarkPage = PageResponse<Bookmark>;

export type FollowedNovelPage = PageResponse<FollowedNovel>;
