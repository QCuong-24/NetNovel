import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { getUserEventReport, getUserNovelInteractions, rebuildUserNovelInteractions } from '../api/data-report-api';

export function useUserEventReport(days: number) {
  return useQuery({
    queryKey: [...queryKeys.dataReports, 'user-events', days],
    queryFn: () => getUserEventReport(days),
  });
}

export function useUserNovelInteractions(params: { page: number; size: number; userId?: number }) {
  return useQuery({
    queryKey: [...queryKeys.dataReports, 'user-novel-interactions', params],
    queryFn: () => getUserNovelInteractions(params),
  });
}

export function useRebuildUserNovelInteractionsMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: rebuildUserNovelInteractions,
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.dataReports });
      toast.success(`Rebuilt ${result.interactionCount} interactions`);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not rebuild interactions'));
    },
  });
}
