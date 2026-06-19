import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { NovelRankingPage, NovelStatistic, NovelStatisticPoint, RankingMetric, RankingParams } from '../types';

function buildRankingParams(params: RankingParams, includePage = true) {
  const searchParams = new URLSearchParams();

  if (params.period === 'month') {
    searchParams.set('month', params.month);
  } else if (params.period === 'year') {
    searchParams.set('year', params.year);
  } else {
    searchParams.set('date', params.date);
  }

  if (includePage) {
    searchParams.set('page', String(params.page ?? 0));
    searchParams.set('size', String(params.size ?? 10));
  }

  return searchParams;
}

export async function getNovelRanking(params: RankingParams) {
  const searchParams = buildRankingParams(params);
  const response = await httpClient.get<NovelRankingPage>(
    `${endpoints.rankings.ranking(params.metric, params.period)}?${searchParams.toString()}`,
  );

  return response.data;
}

export async function getNovelStatistic(params: RankingParams) {
  const searchParams = buildRankingParams(params, false);
  const response = await httpClient.get<NovelStatistic>(
    `${endpoints.rankings.total(params.metric, params.period)}?${searchParams.toString()}`,
  );

  return response.data;
}

export async function getDailyNovelStatisticSeries(metric: RankingMetric, dates: string[]) {
  const statistics = await Promise.all(
    dates.map((date) =>
      getNovelStatistic({
        metric,
        period: 'day',
        date,
        month: date.slice(0, 7),
        year: date.slice(0, 4),
      }),
    ),
  );

  return statistics.map<NovelStatisticPoint>((statistic, index) => ({
    date: dates[index],
    count: statistic.count,
  }));
}
