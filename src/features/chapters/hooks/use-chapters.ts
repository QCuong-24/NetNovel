import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { createChapter, getChapter, getNovelChapters, updateChapter } from '../api/chapter-api';
import type { ChapterPayload } from '../types';

export function useChapter(chapterId?: string) {
  return useQuery({
    queryKey: [...queryKeys.chapters, chapterId],
    queryFn: () => getChapter(chapterId!),
    enabled: Boolean(chapterId),
  });
}

export function useNovelChapters(novelId?: string) {
  return useQuery({
    queryKey: [...queryKeys.chapters, 'novel', novelId],
    queryFn: () => getNovelChapters(novelId!),
    enabled: Boolean(novelId),
  });
}

export function useCreateChapterMutation(novelId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: ChapterPayload) => createChapter(novelId, payload),
    onSuccess: (chapter) => {
      queryClient.invalidateQueries({ queryKey: [...queryKeys.chapters, 'novel', novelId] });
      toast.success(`Created chapter ${chapter.chapterNumber}`);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not create chapter'));
    },
  });
}

export function useUpdateChapterMutation(chapterId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: ChapterPayload) => updateChapter(chapterId, payload),
    onSuccess: (chapter) => {
      queryClient.setQueryData([...queryKeys.chapters, chapterId], chapter);
      queryClient.invalidateQueries({ queryKey: [...queryKeys.chapters, 'novel', String(chapter.novelId)] });
      toast.success(`Updated chapter ${chapter.chapterNumber}`);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update chapter'));
    },
  });
}
