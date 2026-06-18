import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { hasAuthTokens } from '@/features/auth/lib/auth-storage';
import {
  createChapterBookmark,
  createNovelBookmark,
  deleteChapterBookmark,
  deleteNovelBookmark,
  getBookmarks,
  getChapterBookmarkStatus,
  getFollowedNovels,
  getLastReading,
  getNovelBookmarkStatus,
  updateLastRead,
} from '../api/collection-api';
import type { BookmarkKind } from '../types';

export function useLastReading(size = 6) {
  return useQuery({
    queryKey: [...queryKeys.collection, 'lastReading', size],
    queryFn: () => getLastReading({ size }),
  });
}

export function useBookmarks(kind: BookmarkKind, size = 8) {
  return useQuery({
    queryKey: [...queryKeys.collection, 'bookmarks', kind, size],
    queryFn: () => getBookmarks(kind, { size }),
  });
}

export function useFollowedNovels(size = 6) {
  return useQuery({
    queryKey: [...queryKeys.collection, 'followedNovels', size],
    queryFn: () => getFollowedNovels({ size }),
  });
}

export function useUpdateLastReadMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ chapterId, novelId }: { chapterId: string; novelId: string }) => updateLastRead(novelId, chapterId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.collection });
    },
  });
}

export function useNovelBookmarkStatus(novelId?: string) {
  return useQuery({
    queryKey: [...queryKeys.collection, 'bookmarkStatus', 'novel', novelId],
    queryFn: () => getNovelBookmarkStatus(novelId!),
    enabled: Boolean(novelId && hasAuthTokens()),
  });
}

export function useChapterBookmarkStatus(chapterId?: string) {
  return useQuery({
    queryKey: [...queryKeys.collection, 'bookmarkStatus', 'chapter', chapterId],
    queryFn: () => getChapterBookmarkStatus(chapterId!),
    enabled: Boolean(chapterId && hasAuthTokens()),
  });
}

export function useToggleNovelBookmarkMutation(novelId: string, bookmarked?: boolean) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => (bookmarked ? deleteNovelBookmark(novelId) : createNovelBookmark(novelId)),
    onSuccess: () => {
      queryClient.setQueryData([...queryKeys.collection, 'bookmarkStatus', 'novel', novelId], !bookmarked);
      queryClient.invalidateQueries({ queryKey: queryKeys.collection });
      toast.success(bookmarked ? 'Bookmark removed' : 'Novel bookmarked');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update bookmark'));
    },
  });
}

export function useToggleChapterBookmarkMutation(chapterId: string, bookmarked?: boolean) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => (bookmarked ? deleteChapterBookmark(chapterId) : createChapterBookmark(chapterId)),
    onSuccess: () => {
      queryClient.setQueryData([...queryKeys.collection, 'bookmarkStatus', 'chapter', chapterId], !bookmarked);
      queryClient.invalidateQueries({ queryKey: queryKeys.collection });
      toast.success(bookmarked ? 'Bookmark removed' : 'Chapter bookmarked');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update bookmark'));
    },
  });
}
