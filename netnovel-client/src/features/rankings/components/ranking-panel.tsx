import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Bookmark, CalendarDays, Eye, Heart, MessageCircle, Trophy, Users } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { NovelCover } from '@/features/novels/components/novel-cover';
import { formatCount } from '@/features/novels/lib/novel-format';
import { useNovelRanking, useNovelStatistic } from '../hooks/use-rankings';
import type { RankingMetric, RankingParams, RankingPeriod } from '../types';

const metrics: Array<{ icon: typeof Eye; key: RankingMetric }> = [
  { icon: Eye, key: 'views' },
  { icon: Users, key: 'follows' },
  { icon: Heart, key: 'likes' },
  { icon: Bookmark, key: 'bookmarks' },
  { icon: MessageCircle, key: 'comments' },
];

const periods: RankingPeriod[] = ['day', 'week', 'month', 'year'];

function toDateInputValue(date = new Date()) {
  const timezoneOffset = date.getTimezoneOffset() * 60_000;

  return new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 10);
}

function makeInitialParams(): RankingParams {
  const today = toDateInputValue();

  return {
    metric: 'views',
    period: 'day',
    date: today,
    month: today.slice(0, 7),
    year: today.slice(0, 4),
    page: 0,
    size: 10,
  };
}

export function RankingPanel() {
  const { t } = useTranslation();
  const [params, setParams] = useState<RankingParams>(() => makeInitialParams());
  const rankingQuery = useNovelRanking(params);
  const statisticQuery = useNovelStatistic(params);
  const rankingPage = rankingQuery.data;
  const statistic = statisticQuery.data;
  const totalPages = rankingPage?.totalPages ?? 0;
  const currentPage = (rankingPage?.number ?? params.page ?? 0) + 1;
  const pageInfo = totalPages > 0 ? t('rankingPage.pageInfo', { page: currentPage, total: totalPages }) : t('rankingPage.pageInfo', { page: 0, total: 0 });

  const selectedMetric = useMemo(() => metrics.find((metric) => metric.key === params.metric) ?? metrics[0], [params.metric]);
  const SelectedMetricIcon = selectedMetric.icon;

  function updateParams(nextParams: Partial<RankingParams>) {
    setParams((current) => ({ ...current, ...nextParams, page: nextParams.page ?? 0 }));
  }

  function goToPage(page: number) {
    setParams((current) => ({ ...current, page: Math.max(0, page) }));
  }

  return (
    <div className="grid gap-5">
      <Card>
        <CardContent className="grid gap-4 p-4 md:p-6">
          <div className="grid gap-3">
            <span className="text-sm font-bold text-muted-foreground">{t('rankingPage.metric')}</span>
            <div className="flex flex-wrap gap-2">
              {metrics.map((metric) => {
                const Icon = metric.icon;

                return (
                  <Button
                    key={metric.key}
                    type="button"
                    variant={params.metric === metric.key ? 'default' : 'outline'}
                    onClick={() => updateParams({ metric: metric.key })}
                  >
                    <Icon />
                    {t(`rankingPage.metrics.${metric.key}`)}
                  </Button>
                );
              })}
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-[1fr_220px_140px] md:items-end">
            <div className="grid gap-3">
              <span className="text-sm font-bold text-muted-foreground">{t('rankingPage.period')}</span>
              <div className="flex flex-wrap gap-2">
                {periods.map((period) => (
                  <Button
                    key={period}
                    type="button"
                    variant={params.period === period ? 'default' : 'outline'}
                    onClick={() => updateParams({ period })}
                  >
                    <CalendarDays />
                    {t(`rankingPage.periods.${period}`)}
                  </Button>
                ))}
              </div>
            </div>

            <label className="grid gap-2 text-sm font-bold text-muted-foreground">
              {t('rankingPage.time')}
              {params.period === 'month' ? (
                <Input value={params.month} type="month" onChange={(event) => updateParams({ month: event.target.value })} />
              ) : params.period === 'year' ? (
                <Input
                  max="2100"
                  min="2000"
                  value={params.year}
                  type="number"
                  onChange={(event) => updateParams({ year: event.target.value })}
                />
              ) : (
                <Input value={params.date} type="date" onChange={(event) => updateParams({ date: event.target.value })} />
              )}
            </label>

            <label className="grid gap-2 text-sm font-bold text-muted-foreground">
              {t('rankingPage.pageSize')}
              <Input
                max="50"
                min="1"
                value={params.size}
                type="number"
                onChange={(event) => updateParams({ size: Number(event.target.value) || 10 })}
              />
            </label>
          </div>
        </CardContent>
      </Card>

      <Card className="overflow-hidden">
        <CardContent className="flex flex-col gap-3 p-5 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <div className="grid size-11 place-items-center rounded-md bg-primary text-primary-foreground">
              <SelectedMetricIcon />
            </div>
            <div>
              <p className="text-sm font-bold text-muted-foreground">{t('rankingPage.total')}</p>
              <p className="text-3xl font-extrabold">{formatCount(statistic?.count ?? 0)}</p>
            </div>
          </div>
          <Badge variant="secondary">
            {statisticQuery.isLoading
              ? t('rankingPage.loadingTotal')
              : t('rankingPage.range', {
                  start: statistic?.startDate ?? '-',
                  end: statistic?.endDate ?? '-',
                })}
          </Badge>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex-row items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            <Trophy className="size-5 text-primary" />
            {t('rankingPage.results')}
          </CardTitle>
          <Badge variant="outline">{pageInfo}</Badge>
        </CardHeader>
        <CardContent className="grid gap-3">
          {rankingQuery.isLoading ? (
            <div className="grid min-h-40 place-items-center text-sm font-semibold text-muted-foreground">
              {t('rankingPage.loading')}
            </div>
          ) : rankingPage?.content.length ? (
            rankingPage.content.map((item, index) => (
              <Link
                className="grid gap-3 rounded-lg border bg-background p-3 transition hover:border-primary/50 hover:bg-accent sm:grid-cols-[48px_72px_minmax(0,1fr)_120px] sm:items-center"
                key={item.novel.novelId}
                to={`/novels/${item.novel.novelId}`}
              >
                <div className="text-xl font-extrabold text-primary">#{(rankingPage.number * rankingPage.size) + index + 1}</div>
                <NovelCover className="h-24 rounded-md sm:h-24" src={item.novel.coverImageUrl} title={item.novel.title} />
                <div className="grid gap-1">
                  <h3 className="line-clamp-2 text-base font-extrabold">{item.novel.title}</h3>
                  <p className="line-clamp-1 text-sm font-semibold text-muted-foreground">{item.novel.author}</p>
                  <div className="flex flex-wrap gap-1">
                    {item.novel.genres.slice(0, 3).map((genre) => (
                      <Badge key={genre} variant="secondary">
                        {genre}
                      </Badge>
                    ))}
                  </div>
                </div>
                <div className="flex items-center gap-2 text-lg font-extrabold sm:justify-end">
                  <SelectedMetricIcon className="size-5 text-primary" />
                  {formatCount(item.count)}
                </div>
              </Link>
            ))
          ) : (
            <div className="grid min-h-40 place-items-center text-sm font-semibold text-muted-foreground">
              {t('rankingPage.empty')}
            </div>
          )}

          <div className="flex flex-col gap-2 border-t pt-3 sm:flex-row sm:items-center sm:justify-between">
            <span className="text-sm font-bold text-muted-foreground">{pageInfo}</span>
            <div className="flex gap-2">
              <Button
                disabled={(rankingPage?.first ?? true) || rankingQuery.isLoading}
                type="button"
                variant="outline"
                onClick={() => goToPage((params.page ?? 0) - 1)}
              >
                {t('rankingPage.previous')}
              </Button>
              <Button
                disabled={(rankingPage?.last ?? true) || rankingQuery.isLoading}
                type="button"
                variant="outline"
                onClick={() => goToPage((params.page ?? 0) + 1)}
              >
                {t('rankingPage.next')}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
