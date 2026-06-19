import type { PageResponse } from '@/features/novels/types';

export type CommentTargetType = 'novel' | 'chapter';

export type CommentTarget = {
  id: string;
  type: CommentTargetType;
};

export type Comment = {
  commentId: number;
  novelId?: number | null;
  chapterId?: number | null;
  chapterNumber?: number | null;
  userId: number;
  username: string;
  userAvatarUrl?: string | null;
  content: string;
  deleted?: boolean | null;
  replyCount?: number | null;
  createdAt?: string | null;
  lastActivityAt?: string | null;
};

export type CommentPayload = {
  content: string;
};

export type CommentPage = PageResponse<Comment>;
