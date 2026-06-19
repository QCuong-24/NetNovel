import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/config/query-keys';
import { getDailyNovelStatisticSeries, getNovelRanking, getNovelStatistic } from '../api/ranking-api';
import type { RankingMetric, RankingParams } from '../types';

export function useNovelRanking(params: RankingParams) {
  return useQuery({
    queryKey: [...queryKeys.rankings, 'ranking', params],
    queryFn: () => getNovelRanking(params),
  });
}

export function useNovelStatistic(params: RankingParams, enabled = true) {
  return useQuery({
    queryKey: [...queryKeys.rankings, 'total', params],
    queryFn: () => getNovelStatistic(params),
    enabled,
  });
}

export function useDailyNovelStatisticSeries(metric: RankingMetric, dates: string[]) {
  return useQuery({
    queryKey: [...queryKeys.rankings, 'dailySeries', metric, dates],
    queryFn: () => getDailyNovelStatisticSeries(metric, dates),
    enabled: dates.length > 0,
  });
}
