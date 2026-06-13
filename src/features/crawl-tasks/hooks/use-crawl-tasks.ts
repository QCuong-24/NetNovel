import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { createCrawlTask, deleteCrawlChapterRecord, getCrawlChapterRecords, getCrawlTasks } from '../api/crawl-task-api';
import type { CrawlChapterRecordListParams, CrawlTaskCreatePayload, CrawlTaskListParams } from '../types';

export function useCrawlTasks(params: CrawlTaskListParams, enabled = true) {
  return useQuery({
    queryKey: [...queryKeys.crawlTasks, params],
    queryFn: () => getCrawlTasks(params),
    enabled,
  });
}

export function useCreateCrawlTaskMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CrawlTaskCreatePayload) => createCrawlTask(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.crawlTasks });
      toast.success('Crawl task sent');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not send crawl task'));
    },
  });
}

export function useCrawlChapterRecords(params: CrawlChapterRecordListParams, enabled = true) {
  return useQuery({
    queryKey: [...queryKeys.crawlTasks, 'chapterRecords', params],
    queryFn: () => getCrawlChapterRecords(params),
    enabled,
  });
}

export function useDeleteCrawlChapterRecordMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (recordId: string) => deleteCrawlChapterRecord(recordId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.crawlTasks });
      toast.success('Crawl chapter record deleted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not delete crawl chapter record'));
    },
  });
}
