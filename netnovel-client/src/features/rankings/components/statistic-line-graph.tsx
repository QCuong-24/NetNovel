import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Bookmark, Eye, Heart, MessageCircle, TrendingUp, Users } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { formatCount } from '@/features/novels/lib/novel-format';
import { useDailyNovelStatisticSeries } from '../hooks/use-rankings';
import type { RankingMetric } from '../types';

const metrics: Array<{ icon: typeof Eye; key: RankingMetric }> = [
  { icon: Eye, key: 'views' },
  { icon: Users, key: 'follows' },
  { icon: Heart, key: 'likes' },
  { icon: Bookmark, key: 'bookmarks' },
  { icon: MessageCircle, key: 'comments' },
];

const ranges = [7, 14, 30];

function toDateInputValue(date = new Date()) {
  const timezoneOffset = date.getTimezoneOffset() * 60_000;

  return new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 10);
}

function addDays(dateValue: string, days: number) {
  const date = new Date(`${dateValue}T00:00:00`);
  date.setDate(date.getDate() + days);

  return toDateInputValue(date);
}

function makeDateRange(endDate: string, days: number) {
  return Array.from({ length: days }, (_, index) => addDays(endDate, index - days + 1));
}

export function StatisticLineGraph() {
  const { t } = useTranslation();
  const [metric, setMetric] = useState<RankingMetric>('views');
  const [endDate, setEndDate] = useState(() => toDateInputValue());
  const [days, setDays] = useState(14);
  const dates = useMemo(() => makeDateRange(endDate, days), [days, endDate]);
  const seriesQuery = useDailyNovelStatisticSeries(metric, dates);
  const points = seriesQuery.data ?? [];
  const selectedMetric = metrics.find((item) => item.key === metric) ?? metrics[0];
  const SelectedMetricIcon = selectedMetric.icon;
  const total = points.reduce((sum, point) => sum + point.count, 0);
  const peak = points.reduce((max, point) => Math.max(max, point.count), 0);

  return (
    <div className="grid gap-5">
      <Card>
        <CardContent className="grid gap-4 p-4 md:p-6">
          <div className="grid gap-3">
            <span className="text-sm font-bold text-muted-foreground">{t('rankingPage.metric')}</span>
            <div className="flex flex-wrap gap-2">
              {metrics.map((item) => {
                const Icon = item.icon;

                return (
                  <Button
                    key={item.key}
                    type="button"
                    variant={metric === item.key ? 'default' : 'outline'}
                    onClick={() => setMetric(item.key)}
                  >
                    <Icon />
                    {t(`rankingPage.metrics.${item.key}`)}
                  </Button>
                );
              })}
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-[1fr_220px] md:items-end">
            <div className="grid gap-3">
              <span className="text-sm font-bold text-muted-foreground">{t('dashboardPage.statistic.rangeDays')}</span>
              <div className="flex flex-wrap gap-2">
                {ranges.map((range) => (
                  <Button
                    key={range}
                    type="button"
                    variant={days === range ? 'default' : 'outline'}
                    onClick={() => setDays(range)}
                  >
                    {t('dashboardPage.statistic.days', { count: range })}
                  </Button>
                ))}
              </div>
            </div>

            <label className="grid gap-2 text-sm font-bold text-muted-foreground">
              {t('dashboardPage.statistic.endDate')}
              <Input value={endDate} type="date" onChange={(event) => setEndDate(event.target.value)} />
            </label>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex-row items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            <TrendingUp className="size-5 text-primary" />
            {t('dashboardPage.statistic.graphTitle')}
          </CardTitle>
          <Badge variant="outline">
            <SelectedMetricIcon className="mr-1 inline size-3.5" />
            {t(`rankingPage.metrics.${metric}`)}
          </Badge>
        </CardHeader>
        <CardContent className="grid gap-5">
          <div className="grid gap-3 sm:grid-cols-3">
            <MetricCard label={t('dashboardPage.statistic.total')} value={formatCount(total)} />
            <MetricCard label={t('dashboardPage.statistic.peak')} value={formatCount(peak)} />
            <MetricCard label={t('dashboardPage.statistic.average')} value={formatCount(points.length ? Math.round(total / points.length) : 0)} />
          </div>

          {seriesQuery.isLoading ? (
            <div className="grid min-h-72 place-items-center text-sm font-semibold text-muted-foreground">
              {t('dashboardPage.statistic.loadingSeries')}
            </div>
          ) : points.length ? (
            <LineChart points={points} />
          ) : (
            <div className="grid min-h-72 place-items-center text-sm font-semibold text-muted-foreground">
              {t('dashboardPage.statistic.emptySeries')}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border bg-background p-4">
      <p className="text-sm font-bold text-muted-foreground">{label}</p>
      <p className="mt-2 text-2xl font-extrabold">{value}</p>
    </div>
  );
}

function LineChart({ points }: { points: Array<{ date: string; count: number }> }) {
  const width = 900;
  const height = 280;
  const padding = 32;
  const maxValue = Math.max(...points.map((point) => point.count), 1);
  const coordinates = points.map((point, index) => {
    const x = points.length === 1 ? width / 2 : padding + (index * (width - padding * 2)) / (points.length - 1);
    const y = height - padding - (point.count / maxValue) * (height - padding * 2);

    return { ...point, x, y };
  });
  const polyline = coordinates.map((point) => `${point.x},${point.y}`).join(' ');
  const area = `${padding},${height - padding} ${polyline} ${width - padding},${height - padding}`;

  return (
    <div className="overflow-x-auto rounded-lg border bg-background p-3">
      <svg aria-label="Statistic line graph" className="min-w-[720px]" viewBox={`0 0 ${width} ${height}`}>
        <defs>
          <linearGradient id="statistic-area" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stopColor="hsl(var(--primary))" stopOpacity="0.26" />
            <stop offset="100%" stopColor="hsl(var(--primary))" stopOpacity="0.02" />
          </linearGradient>
        </defs>
        {[0, 1, 2, 3].map((line) => {
          const y = padding + (line * (height - padding * 2)) / 3;

          return <line key={line} stroke="hsl(var(--border))" strokeDasharray="4 6" x1={padding} x2={width - padding} y1={y} y2={y} />;
        })}
        <polygon fill="url(#statistic-area)" points={area} />
        <polyline fill="none" points={polyline} stroke="hsl(var(--primary))" strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" />
        {coordinates.map((point) => (
          <g key={point.date}>
            <circle cx={point.x} cy={point.y} fill="hsl(var(--background))" r="5" stroke="hsl(var(--primary))" strokeWidth="3" />
            <title>{`${point.date}: ${point.count}`}</title>
          </g>
        ))}
        {coordinates.map((point, index) => {
          const shouldShow = index === 0 || index === coordinates.length - 1 || index % Math.ceil(coordinates.length / 6) === 0;

          return shouldShow ? (
            <text
              fill="hsl(var(--muted-foreground))"
              fontSize="12"
              fontWeight="700"
              key={point.date}
              textAnchor="middle"
              x={point.x}
              y={height - 8}
            >
              {point.date.slice(5)}
            </text>
          ) : null;
        })}
      </svg>
    </div>
  );
}
