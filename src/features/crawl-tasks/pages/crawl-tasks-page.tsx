import { ChevronFirst, ChevronLast, ChevronLeft, ChevronRight, RefreshCcw, RefreshCw, Send, ShieldAlert, Trash2 } from 'lucide-react';
import { FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import type { User } from '@/features/auth/types';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import {
  useCreateCrawlTaskMutation,
  useCrawlChapterRecords,
  useCrawlTasks,
  useDeleteCrawlChapterRecordMutation,
} from '../hooks/use-crawl-tasks';
import type { CrawlChapterStatus, CrawlTaskStatus } from '../types';

const taskStatuses: CrawlTaskStatus[] = [
  'PENDING',
  'RUNNING',
  'SUCCESS',
  'PARTIAL_SUCCESS',
  'FAILED',
  'SKIPPED_UNSUPPORTED_SOURCE',
  'CANCELLED',
];

const chapterStatuses: CrawlChapterStatus[] = ['SUCCESS', 'FAILED'];

function isAdmin(user?: User) {
  return Boolean(user?.roles?.includes('ADMIN'));
}

function getTaskStatusVariant(status: CrawlTaskStatus) {
  if (status === 'SUCCESS') {
    return 'default';
  }
  if (status === 'FAILED' || status === 'CANCELLED' || status === 'SKIPPED_UNSUPPORTED_SOURCE') {
    return 'outline';
  }

  return 'secondary';
}

function getChapterStatusVariant(status: CrawlChapterStatus) {
  return status === 'SUCCESS' ? 'default' : 'outline';
}

function toIsoLocalDateTime(value: string) {
  return value ? value : undefined;
}

function clampPage(page: number, totalPages: number) {
  if (totalPages <= 0) {
    return 0;
  }

  return Math.min(Math.max(page, 0), totalPages - 1);
}

export function CrawlTasksPage() {
  const { t } = useTranslation();
  const { data: user, isLoading: isUserLoading } = useCurrentUser();
  const canAccessTasks = canManageNovels(user);
  const canAccessRecords = isAdmin(user);
  const [activeTab, setActiveTab] = useState<'tasks' | 'records'>('tasks');

  if (isUserLoading) {
    return (
      <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">
        {t('crawlTasks.loading')}
      </div>
    );
  }

  if (!canAccessTasks) {
    return <PermissionCard description={t('crawlTasks.noPermissionDescription')} title={t('crawlTasks.noPermission')} />;
  }

  return (
    <div className="grid gap-6">
      <header className="grid gap-2">
        <p className="text-sm font-semibold uppercase text-primary">{t('crawlTasks.eyebrow')}</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-4xl">{t('crawlTasks.title')}</h1>
        <p className="max-w-3xl text-sm leading-6 text-muted-foreground">{t('crawlTasks.description')}</p>
      </header>

      <div className="flex w-full flex-wrap gap-2 rounded-lg border bg-card p-1">
        <Button
          className="flex-1 sm:flex-none"
          type="button"
          variant={activeTab === 'tasks' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('tasks')}
        >
          {t('crawlTasks.tabs.tasks')}
        </Button>
        <Button
          className="flex-1 sm:flex-none"
          type="button"
          variant={activeTab === 'records' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('records')}
        >
          {t('crawlTasks.tabs.records')}
        </Button>
      </div>

      {activeTab === 'tasks' ? <CrawlTaskPanel canAccess={canAccessTasks} /> : null}
      {activeTab === 'records' ? (
        canAccessRecords ? (
          <ChapterRecordsPanel />
        ) : (
          <PermissionCard
            description={t('crawlTasks.recordsNoPermissionDescription')}
            title={t('crawlTasks.recordsNoPermission')}
          />
        )
      ) : null}
    </div>
  );
}

function PermissionCard({ description, title }: { description: string; title: string }) {
  return (
    <Card>
      <CardContent className="grid gap-4 p-6">
        <ShieldAlert className="size-10 text-destructive" />
        <div className="grid gap-1">
          <p className="text-lg font-bold">{title}</p>
          <p className="text-sm text-muted-foreground">{description}</p>
        </div>
      </CardContent>
    </Card>
  );
}

function CrawlTaskPanel({ canAccess }: { canAccess: boolean }) {
  const { t } = useTranslation();
  const [url, setUrl] = useState('');
  const [status, setStatus] = useState<CrawlTaskStatus | ''>('');
  const [scope, setScope] = useState<'all' | 'personal'>('all');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const crawlTasksQuery = useCrawlTasks(
    {
      page,
      size: pageSize,
      status: status || undefined,
      personal: scope === 'personal',
    },
    canAccess,
  );
  const createCrawlTaskMutation = useCreateCrawlTaskMutation();

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedUrl = url.trim();

    if (!trimmedUrl) {
      return;
    }

    await createCrawlTaskMutation.mutateAsync({ url: trimmedUrl });
    setUrl('');
    setPage(0);
  }

  async function resendCrawlTask(taskUrl: string) {
    await createCrawlTaskMutation.mutateAsync({ url: taskUrl });
    setPage(0);
  }

  function handleStatusChange(nextStatus: string) {
    setStatus(nextStatus as CrawlTaskStatus | '');
    setPage(0);
  }

  function handleScopeChange(nextScope: string) {
    setScope(nextScope === 'personal' ? 'personal' : 'all');
    setPage(0);
  }

  const taskPage = crawlTasksQuery.data;
  const tasks = taskPage?.content ?? [];

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>{t('crawlTasks.sendTask')}</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="grid gap-3 md:grid-cols-[minmax(0,1fr)_auto]" onSubmit={handleSubmit}>
            <Input
              placeholder={t('crawlTasks.urlPlaceholder')}
              type="url"
              value={url}
              onChange={(event) => setUrl(event.target.value)}
            />
            <Button disabled={!url.trim() || createCrawlTaskMutation.isPending} type="submit">
              <Send />
              {createCrawlTaskMutation.isPending ? t('crawlTasks.sending') : t('crawlTasks.send')}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="grid gap-4 lg:flex lg:flex-row lg:items-center lg:justify-between">
          <div>
            <CardTitle>{t('crawlTasks.taskList')}</CardTitle>
            <p className="mt-1 text-sm text-muted-foreground">
              {t('crawlTasks.total', { count: taskPage?.totalElements ?? 0 })}
            </p>
          </div>
          <div className="grid gap-2 sm:grid-cols-[minmax(0,180px)_minmax(0,180px)_auto]">
            <select
              className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
              value={status}
              onChange={(event) => handleStatusChange(event.target.value)}
            >
              <option value="">{t('crawlTasks.filters.allStatuses')}</option>
              {taskStatuses.map((taskStatus) => (
                <option key={taskStatus} value={taskStatus}>
                  {t(`crawlTasks.status.${taskStatus}`)}
                </option>
              ))}
            </select>
            <select
              className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
              value={scope}
              onChange={(event) => handleScopeChange(event.target.value)}
            >
              <option value="all">{t('crawlTasks.filters.allTasks')}</option>
              <option value="personal">{t('crawlTasks.filters.personal')}</option>
            </select>
            <Button
              disabled={crawlTasksQuery.isFetching}
              type="button"
              variant="outline"
              onClick={() => crawlTasksQuery.refetch()}
            >
              <RefreshCw className={crawlTasksQuery.isFetching ? 'animate-spin' : undefined} />
              {t('crawlTasks.reload')}
            </Button>
          </div>
        </CardHeader>
        <CardContent className="grid gap-3">
          {crawlTasksQuery.isLoading ? <p className="text-sm text-muted-foreground">{t('crawlTasks.loading')}</p> : null}
          {!crawlTasksQuery.isLoading && !tasks.length ? (
            <p className="rounded-lg border border-dashed p-4 text-sm text-muted-foreground">
              {t('crawlTasks.empty')}
            </p>
          ) : null}
          {tasks.length ? (
            <div className="overflow-x-auto rounded-lg border">
              <table className="w-full min-w-[980px] border-collapse text-left text-sm">
                <thead className="bg-muted/60 text-xs uppercase text-muted-foreground">
                  <tr>
                    <th className="px-3 py-3">{t('crawlTasks.tasks.columns.id')}</th>
                    <th className="px-3 py-3">{t('crawlTasks.tasks.columns.status')}</th>
                    <th className="px-3 py-3">{t('crawlTasks.tasks.columns.url')}</th>
                    <th className="px-3 py-3">{t('crawlTasks.tasks.columns.requestedBy')}</th>
                    <th className="px-3 py-3">{t('crawlTasks.tasks.columns.created')}</th>
                    <th className="px-3 py-3">{t('crawlTasks.tasks.columns.started')}</th>
                    <th className="px-3 py-3">{t('crawlTasks.tasks.columns.finished')}</th>
                    <th className="px-3 py-3 text-right">{t('crawlTasks.tasks.columns.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map((task) => (
                    <tr className="border-t align-top" key={task.id}>
                      <td className="px-3 py-3 font-bold">#{task.id}</td>
                      <td className="px-3 py-3">
                        <Badge variant={getTaskStatusVariant(task.status)}>{t(`crawlTasks.status.${task.status}`)}</Badge>
                      </td>
                      <td className="max-w-80 px-3 py-3">
                        <a
                          className="line-clamp-2 break-all font-semibold text-primary hover:underline"
                          href={task.url}
                          rel="noreferrer"
                          target="_blank"
                        >
                          {task.url}
                        </a>
                        {task.errorMessage ? (
                          <p className="mt-2 line-clamp-2 text-xs text-destructive">{task.errorMessage}</p>
                        ) : null}
                      </td>
                      <td className="px-3 py-3 text-muted-foreground">
                        {task.requestedByUserId ?? t('crawlTasks.system')}
                      </td>
                      <td className="px-3 py-3 text-muted-foreground">
                        {task.createAt ? formatDateTime(task.createAt) : '-'}
                      </td>
                      <td className="px-3 py-3 text-muted-foreground">
                        {task.startedAt ? formatDateTime(task.startedAt) : '-'}
                      </td>
                      <td className="px-3 py-3 text-muted-foreground">
                        {task.finishedAt ? formatDateTime(task.finishedAt) : '-'}
                      </td>
                      <td className="px-3 py-3 text-right">
                        <Button
                          aria-label={t('crawlTasks.tasks.resend')}
                          disabled={createCrawlTaskMutation.isPending}
                          size="icon"
                          type="button"
                          variant="outline"
                          onClick={() => resendCrawlTask(task.url)}
                        >
                          <RefreshCcw />
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}

          <Pagination
            first={taskPage?.first ?? true}
            last={taskPage?.last ?? true}
            page={taskPage?.number ?? page}
            pageSize={pageSize}
            totalPages={taskPage?.totalPages ?? 0}
            onFirst={() => setPage(0)}
            onLast={() => setPage(Math.max(0, (taskPage?.totalPages ?? 1) - 1))}
            onNext={() => setPage(page + 1)}
            onPageChange={(nextPage) => setPage(clampPage(nextPage, taskPage?.totalPages ?? 0))}
            onPageSizeChange={(nextPageSize) => {
              setPageSize(nextPageSize);
              setPage(0);
            }}
            onPrevious={() => setPage(Math.max(0, page - 1))}
          />
        </CardContent>
      </Card>
    </>
  );
}

function ChapterRecordsPanel() {
  const { t } = useTranslation();
  const [status, setStatus] = useState<CrawlChapterStatus | ''>('');
  const [novelId, setNovelId] = useState('');
  const [start, setStart] = useState('');
  const [end, setEnd] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const recordsQuery = useCrawlChapterRecords({
    page,
    size: pageSize,
    status: status || undefined,
    novelId: novelId.trim() || undefined,
    start: toIsoLocalDateTime(start),
    end: toIsoLocalDateTime(end),
  });
  const deleteRecordMutation = useDeleteCrawlChapterRecordMutation();
  const recordPage = recordsQuery.data;
  const records = recordPage?.content ?? [];

  function resetPageAndSetStatus(nextStatus: string) {
    setStatus(nextStatus as CrawlChapterStatus | '');
    setPage(0);
  }

  function handleDelete(recordId: number) {
    if (!window.confirm(t('crawlTasks.records.confirmDelete'))) {
      return;
    }

    deleteRecordMutation.mutate(String(recordId));
  }

  return (
    <Card>
      <CardHeader className="grid gap-4">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <CardTitle>{t('crawlTasks.records.title')}</CardTitle>
            <p className="mt-1 text-sm text-muted-foreground">
              {t('crawlTasks.records.total', { count: recordPage?.totalElements ?? 0 })}
            </p>
          </div>
          <Button disabled={recordsQuery.isFetching} type="button" variant="outline" onClick={() => recordsQuery.refetch()}>
            <RefreshCw className={recordsQuery.isFetching ? 'animate-spin' : undefined} />
            {t('crawlTasks.reload')}
          </Button>
        </div>

        <div className="grid gap-2 md:grid-cols-4">
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={status}
            onChange={(event) => resetPageAndSetStatus(event.target.value)}
          >
            <option value="">{t('crawlTasks.filters.allStatuses')}</option>
            {chapterStatuses.map((chapterStatus) => (
              <option key={chapterStatus} value={chapterStatus}>
                {t(`crawlTasks.chapterStatus.${chapterStatus}`)}
              </option>
            ))}
          </select>
          <Input
            min="1"
            placeholder={t('crawlTasks.records.novelIdPlaceholder')}
            type="number"
            value={novelId}
            onChange={(event) => {
              setNovelId(event.target.value);
              setPage(0);
            }}
          />
          <Input
            aria-label={t('crawlTasks.records.start')}
            type="datetime-local"
            value={start}
            onChange={(event) => {
              setStart(event.target.value);
              setPage(0);
            }}
          />
          <Input
            aria-label={t('crawlTasks.records.end')}
            type="datetime-local"
            value={end}
            onChange={(event) => {
              setEnd(event.target.value);
              setPage(0);
            }}
          />
        </div>
      </CardHeader>
      <CardContent className="grid gap-3">
        {recordsQuery.isLoading ? <p className="text-sm text-muted-foreground">{t('crawlTasks.records.loading')}</p> : null}
        {!recordsQuery.isLoading && !records.length ? (
          <p className="rounded-lg border border-dashed p-4 text-sm text-muted-foreground">
            {t('crawlTasks.records.empty')}
          </p>
        ) : null}

        {records.length ? (
          <div className="overflow-x-auto rounded-lg border">
            <table className="w-full min-w-[980px] border-collapse text-left text-sm">
              <thead className="bg-muted/60 text-xs uppercase text-muted-foreground">
                <tr>
                  <th className="px-3 py-3">{t('crawlTasks.records.columns.id')}</th>
                  <th className="px-3 py-3">{t('crawlTasks.records.columns.status')}</th>
                  <th className="px-3 py-3">{t('crawlTasks.records.columns.source')}</th>
                  <th className="px-3 py-3">{t('crawlTasks.records.columns.novel')}</th>
                  <th className="px-3 py-3">{t('crawlTasks.records.columns.chapter')}</th>
                  <th className="px-3 py-3">{t('crawlTasks.records.columns.crawledAt')}</th>
                  <th className="px-3 py-3 text-right">{t('crawlTasks.records.columns.actions')}</th>
                </tr>
              </thead>
              <tbody>
                {records.map((record) => (
                  <tr className="border-t align-top" key={record.id}>
                    <td className="px-3 py-3 font-bold">#{record.id}</td>
                    <td className="px-3 py-3">
                      <Badge variant={getChapterStatusVariant(record.status)}>
                        {t(`crawlTasks.chapterStatus.${record.status}`)}
                      </Badge>
                    </td>
                    <td className="max-w-72 px-3 py-3">
                      <p className="font-semibold">{record.sourceName}</p>
                      <a
                        className="line-clamp-2 break-all text-xs text-primary hover:underline"
                        href={record.sourceChapterUrl}
                        rel="noreferrer"
                        target="_blank"
                      >
                        {record.sourceChapterUrl}
                      </a>
                      {record.errorMessage ? (
                        <p className="mt-2 line-clamp-2 text-xs text-destructive">{record.errorMessage}</p>
                      ) : null}
                    </td>
                    <td className="px-3 py-3">
                      <p className="font-semibold">{record.novelTitle ?? '-'}</p>
                      <p className="text-xs text-muted-foreground">{record.novelId ? `ID ${record.novelId}` : '-'}</p>
                    </td>
                    <td className="px-3 py-3">
                      <p className="font-semibold">
                        {record.chapterNumber ? `${t('chapters.chapter')} ${record.chapterNumber}` : '-'}
                      </p>
                      <p className="text-xs text-muted-foreground">{record.chapterTitle ?? '-'}</p>
                    </td>
                    <td className="px-3 py-3 text-muted-foreground">
                      {record.crawledAt ? formatDateTime(record.crawledAt) : '-'}
                    </td>
                    <td className="px-3 py-3 text-right">
                      <Button
                        aria-label={t('crawlTasks.records.delete')}
                        disabled={deleteRecordMutation.isPending}
                        size="icon"
                        type="button"
                        variant="destructive"
                        onClick={() => handleDelete(record.id)}
                      >
                        <Trash2 />
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}

        <Pagination
          first={recordPage?.first ?? true}
          last={recordPage?.last ?? true}
          page={recordPage?.number ?? page}
          pageSize={pageSize}
          totalPages={recordPage?.totalPages ?? 0}
          onFirst={() => setPage(0)}
          onLast={() => setPage(Math.max(0, (recordPage?.totalPages ?? 1) - 1))}
          onNext={() => setPage(page + 1)}
          onPageChange={(nextPage) => setPage(clampPage(nextPage, recordPage?.totalPages ?? 0))}
          onPageSizeChange={(nextPageSize) => {
            setPageSize(nextPageSize);
            setPage(0);
          }}
          onPrevious={() => setPage(Math.max(0, page - 1))}
        />
      </CardContent>
    </Card>
  );
}

function Pagination({
  first,
  last,
  onFirst,
  onLast,
  onNext,
  onPageChange,
  onPageSizeChange,
  onPrevious,
  page,
  pageSize,
  totalPages,
}: {
  first: boolean;
  last: boolean;
  onFirst: () => void;
  onLast: () => void;
  onNext: () => void;
  onPageChange: (page: number) => void;
  onPageSizeChange: (pageSize: number) => void;
  onPrevious: () => void;
  page: number;
  pageSize: number;
  totalPages: number;
}) {
  const { t } = useTranslation();
  const currentPage = totalPages <= 0 ? 0 : page + 1;

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 pt-2">
      <p className="text-sm text-muted-foreground">
        {t('crawlTasks.pageInfo', { page: currentPage, total: Math.max(totalPages, 1) })}
      </p>
      <div className="flex flex-wrap items-center gap-2">
        <label className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
          {t('crawlTasks.pageSize')}
          <Input
            className="h-9 w-20"
            min={1}
            type="number"
            value={pageSize}
            onChange={(event) => {
              const nextPageSize = Number(event.target.value);

              if (Number.isFinite(nextPageSize) && nextPageSize > 0) {
                onPageSizeChange(nextPageSize);
              }
            }}
          />
        </label>
        <Button
          aria-label={t('crawlTasks.firstPage')}
          disabled={first || totalPages <= 1}
          size="icon"
          type="button"
          variant="outline"
          onClick={onFirst}
        >
          <ChevronFirst />
        </Button>
        <Button
          aria-label={t('common.back')}
          disabled={first || totalPages <= 1}
          size="icon"
          type="button"
          variant="outline"
          onClick={onPrevious}
        >
          <ChevronLeft />
        </Button>
        <label className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
          {t('crawlTasks.page')}
          <Input
            className="h-9 w-20"
            min={1}
            max={Math.max(totalPages, 1)}
            type="number"
            value={currentPage}
            onChange={(event) => {
              const nextPage = Number(event.target.value);

              if (Number.isFinite(nextPage) && nextPage > 0) {
                onPageChange(nextPage - 1);
              }
            }}
          />
        </label>
        <Button
          aria-label={t('chapters.next')}
          disabled={last || totalPages <= 1}
          size="icon"
          type="button"
          variant="outline"
          onClick={onNext}
        >
          <ChevronRight />
        </Button>
        <Button
          aria-label={t('crawlTasks.lastPage')}
          disabled={last || totalPages <= 1}
          size="icon"
          type="button"
          variant="outline"
          onClick={onLast}
        >
          <ChevronLast />
        </Button>
      </div>
    </div>
  );
}
