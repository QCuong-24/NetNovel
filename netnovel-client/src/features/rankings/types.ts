import type { Novel, PageResponse } from '@/features/novels/types';

export type RankingMetric = 'views' | 'follows' | 'likes' | 'bookmarks' | 'comments';
export type RankingPeriod = 'day' | 'week' | 'month' | 'year';

export type RankingParams = {
  metric: RankingMetric;
  period: RankingPeriod;
  date: string;
  month: string;
  year: string;
  page?: number;
  size?: number;
};

export type NovelRanking = {
  novel: Novel;
  count: number;
};

export type NovelStatistic = {
  metric: string;
  period: string;
  startDate?: string | null;
  endDate?: string | null;
  count: number;
};

export type NovelStatisticPoint = {
  date: string;
  count: number;
};

export type NovelRankingPage = PageResponse<NovelRanking>;
