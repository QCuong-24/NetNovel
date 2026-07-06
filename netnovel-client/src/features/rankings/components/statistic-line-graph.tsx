import { useQueries } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Bookmark, Eye, Heart, MessageCircle, TrendingUp, Users } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { formatCount } from '@/features/novels/lib/novel-format';
import { queryKeys } from '@/config/query-keys';
import { getDailyNovelStatisticSeries } from '../api/ranking-api';
import type { NovelStatisticPoint, RankingMetric } from '../types';

const metrics: Array<{ color: string; icon: typeof Eye; key: RankingMetric }> = [
  { color: 'hsl(var(--primary))', icon: Eye, key: 'views' },
  { color: 'hsl(168 76% 42%)', icon: Users, key: 'follows' },
  { color: 'hsl(346 84% 61%)', icon: Heart, key: 'likes' },
  { color: 'hsl(38 92% 50%)', icon: Bookmark, key: 'bookmarks' },
  { color: 'hsl(258 90% 66%)', icon: MessageCircle, key: 'comments' },
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
  const [visibleMetrics, setVisibleMetrics] = useState<RankingMetric[]>(['views', 'follows', 'likes']);
  const [endDate, setEndDate] = useState(() => toDateInputValue());
  const [days, setDays] = useState(14);
  const dates = useMemo(() => makeDateRange(endDate, days), [days, endDate]);
  const seriesQueries = useQueries({
    queries: metrics.map((metric) => ({
      queryKey: [...queryKeys.rankings, 'dailySeries', metric.key, dates],
      queryFn: () => getDailyNovelStatisticSeries(metric.key, dates),
      enabled: dates.length > 0,
    })),
  });
  const series = metrics.map((metric, index) => ({
    ...metric,
    points: seriesQueries[index].data ?? [],
  }));
  const visibleSeries = series.filter((item) => visibleMetrics.includes(item.key));
  const isLoading = seriesQueries.some((query) => query.isLoading);
  const hasPoints = visibleSeries.some((item) => item.points.length > 0);
  const total = visibleSeries.reduce((sum, item) => sum + item.points.reduce((itemSum, point) => itemSum + point.count, 0), 0);
  const peak = visibleSeries.reduce((max, item) => Math.max(max, ...item.points.map((point) => point.count), 0), 0);
  const pointCount = visibleSeries.reduce((sum, item) => sum + item.points.length, 0);

  function toggleMetric(metric: RankingMetric) {
    setVisibleMetrics((current) => {
      if (current.includes(metric)) {
        return current.length === 1 ? current : current.filter((item) => item !== metric);
      }

      return [...current, metric];
    });
  }

  return (
    <div className="grid gap-5">
      <Card>
        <CardContent className="grid gap-4 p-4 md:p-6">
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
          <div className="flex flex-wrap justify-end gap-2">
            {series.map((item) => (
              <button
                className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-bold transition-opacity ${
                  visibleMetrics.includes(item.key) ? 'opacity-100' : 'opacity-45'
                }`}
                key={item.key}
                type="button"
                onClick={() => toggleMetric(item.key)}
              >
                <span className="size-2.5 rounded-full" style={{ backgroundColor: item.color }} />
                {t(`rankingPage.metrics.${item.key}`)}
              </button>
            ))}
          </div>
        </CardHeader>
        <CardContent className="grid gap-5">
          <div className="grid gap-3 sm:grid-cols-3">
            <MetricCard label={t('dashboardPage.statistic.total')} value={formatCount(total)} />
            <MetricCard label={t('dashboardPage.statistic.peak')} value={formatCount(peak)} />
            <MetricCard label={t('dashboardPage.statistic.average')} value={formatCount(pointCount ? Math.round(total / pointCount) : 0)} />
          </div>

          {isLoading ? (
            <div className="grid min-h-72 place-items-center text-sm font-semibold text-muted-foreground">
              {t('dashboardPage.statistic.loadingSeries')}
            </div>
          ) : hasPoints ? (
            <LineChart series={visibleSeries} />
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

function LineChart({
  series,
}: {
  series: Array<{ color: string; key: RankingMetric; points: NovelStatisticPoint[] }>;
}) {
  const { t } = useTranslation();
  const [hoveredPoint, setHoveredPoint] = useState<{
    date: string;
    metric: RankingMetric;
    count: number;
    x: number;
    y: number;
  } | null>(null);
  const width = 900;
  const height = 280;
  const padding = 40;
  const allPoints = series.flatMap((item) => item.points);
  const maxValue = Math.max(...allPoints.map((point) => point.count), 1);
  const yTicks = [0, 1, 2, 3].map((tick) => {
    const ratio = tick / 3;
    const value = Math.round(maxValue * (1 - ratio));
    const y = padding + ratio * (height - padding * 2);

    return { value, y };
  });
  const dateCount = Math.max(...series.map((item) => item.points.length), 1);
  const chartSeries = series.map((item) => {
    const coordinates = item.points.map((point, index) => {
      const x = item.points.length === 1 ? width / 2 : padding + (index * (width - padding * 2)) / (item.points.length - 1);
      const y = height - padding - (point.count / maxValue) * (height - padding * 2);

      return { ...point, metric: item.key, x, y };
    });

    return {
      ...item,
      coordinates,
      polyline: coordinates.map((point) => `${point.x},${point.y}`).join(' '),
    };
  });
  const labelPoints = chartSeries[0]?.coordinates ?? [];

  return (
    <div className="relative overflow-x-auto rounded-lg border bg-background p-3">
      <svg aria-label="Statistic line graph" className="min-w-[720px]" viewBox={`0 0 ${width} ${height}`}>
        {yTicks.map((tick) => (
          <g key={`${tick.y}-${tick.value}`}>
            <line stroke="hsl(var(--border))" strokeDasharray="4 6" x1={padding} x2={width - padding} y1={tick.y} y2={tick.y} />
            <text
              fill="hsl(var(--muted-foreground))"
              fontSize="12"
              fontWeight="700"
              textAnchor="end"
              x={padding - 10}
              y={tick.y + 4}
            >
              {formatCount(tick.value)}
            </text>
          </g>
        ))}
        <line stroke="hsl(var(--border))" strokeWidth="2" x1={padding} x2={padding} y1={padding} y2={height - padding} />
        <line stroke="hsl(var(--border))" strokeWidth="2" x1={padding} x2={width - padding} y1={height - padding} y2={height - padding} />
        {chartSeries.map((item) => (
          <polyline
            className="chart-line-draw"
            fill="none"
            key={item.key}
            points={item.polyline}
            stroke={item.color}
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="3.5"
          />
        ))}
        {chartSeries.flatMap((item) =>
          item.coordinates.map((point) => (
            <g key={`${item.key}-${point.date}`}>
              <circle
                className="chart-point-pop cursor-pointer transition-[r]"
                cx={point.x}
                cy={point.y}
                fill="hsl(var(--background))"
                r={hoveredPoint?.date === point.date && hoveredPoint.metric === item.key ? 7 : 5}
                stroke={item.color}
                strokeWidth="3"
                tabIndex={0}
                onBlur={() => setHoveredPoint(null)}
                onFocus={() => setHoveredPoint({ count: point.count, date: point.date, metric: item.key, x: point.x, y: point.y })}
                onMouseEnter={() => setHoveredPoint({ count: point.count, date: point.date, metric: item.key, x: point.x, y: point.y })}
                onMouseLeave={() => setHoveredPoint(null)}
              />
            </g>
          )),
        )}
        {labelPoints.map((point, index) => {
          const shouldShow = index === 0 || index === labelPoints.length - 1 || index % Math.ceil(dateCount / 6) === 0;

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
      {hoveredPoint ? (
        <div
          className="pointer-events-none absolute rounded-lg border bg-popover px-3 py-2 text-xs text-popover-foreground shadow-xl"
          style={{
            left: `clamp(0.75rem, ${(hoveredPoint.x / width) * 100}%, calc(100% - 11rem))`,
            top: `clamp(0.75rem, ${hoveredPoint.y - 12}px, 14rem)`,
          }}
        >
          <p className="font-extrabold">{hoveredPoint.date}</p>
          <p className="mt-1 font-semibold text-muted-foreground">
            {t(`rankingPage.metrics.${hoveredPoint.metric}`)}: {formatCount(hoveredPoint.count)}
          </p>
        </div>
      ) : null}
    </div>
  );
}
