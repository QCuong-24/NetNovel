import { ArrowLeft, BookOpen, Bookmark, ChevronDown, Eye, Heart, Pencil, Plus, RefreshCw, RotateCcw, Trash2, Users } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { routes } from '@/config/routes';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { ChapterListSection } from '@/features/chapters/components/chapter-list-section';
import { useNovelChapters } from '@/features/chapters/hooks/use-chapters';
import { CommentSection } from '@/features/comments/components/comment-section';
import { useCreateCrawlTaskMutation } from '@/features/crawl-tasks/hooks/use-crawl-tasks';
import { NovelCover } from '../components/novel-cover';
import { NovelForm } from '../components/novel-form';
import { SimilarNovelsSection } from '../components/similar-novels-section';
import { formatCount, formatDateTime } from '../lib/novel-format';
import { canManageNovels } from '../lib/novel-permissions';
import {
  useDeleteNovelMutation,
  useMyNovelInteraction,
  useNovel,
  useNovelTags,
  useRecordNovelViewMutation,
  useToggleNovelFollowMutation,
  useToggleNovelBookmarkMutation,
  useToggleNovelLikeMutation,
  useUpdateNovelMutation,
} from '../hooks/use-novels';
import type { NovelPayload } from '../types';

const metricItems = [
  { key: 'views', labelKey: 'novelPages.metrics.views', icon: Eye },
  { key: 'likes', labelKey: 'novelPages.metrics.likes', icon: Heart },
  { key: 'bookmarks', labelKey: 'novelPages.metrics.bookmarks', icon: Bookmark },
  { key: 'follows', labelKey: 'novelPages.metrics.follows', icon: Users },
] as const;

const crawledSourcePattern = /\[Crawled Source:\s*(https?:\/\/[^\]]+)\]/i;

function extractCrawledSourceUrl(description: string) {
  const sourceUrl = description.match(crawledSourcePattern)?.[1]?.trim();

  if (!sourceUrl) {
    return null;
  }

  try {
    return new URL(sourceUrl).toString();
  } catch {
    return null;
  }
}

export function NovelDetailPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { novelId } = useParams();
  const [isEditing, setIsEditing] = useState(false);
  const [isTagsOpen, setIsTagsOpen] = useState(false);
  const [deleteCountdown, setDeleteCountdown] = useState<number | null>(null);
  const deleteIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const deleteTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const viewedNovelRef = useRef<string | null>(null);
  const { data: user } = useCurrentUser();
  const canEdit = canManageNovels(user);
  const { data: novel, isError, isLoading } = useNovel(novelId);
  const { data: chapters = [], isLoading: isChaptersLoading } = useNovelChapters(novelId);
  const novelTagsQuery = useNovelTags(canEdit ? novelId : undefined);
  const { data: interaction } = useMyNovelInteraction(novelId);
  const updateNovelMutation = useUpdateNovelMutation(novelId ?? '');
  const deleteNovelMutation = useDeleteNovelMutation(novelId ?? '');
  const followMutation = useToggleNovelFollowMutation(novelId ?? '');
  const likeMutation = useToggleNovelLikeMutation(novelId ?? '');
  const bookmarkMutation = useToggleNovelBookmarkMutation(novelId ?? '');
  const recordNovelViewMutation = useRecordNovelViewMutation(novelId);
  const createCrawlTaskMutation = useCreateCrawlTaskMutation();

  useEffect(() => {
    return () => {
      if (deleteIntervalRef.current) {
        clearInterval(deleteIntervalRef.current);
      }
      if (deleteTimeoutRef.current) {
        clearTimeout(deleteTimeoutRef.current);
      }
    };
  }, []);

  useEffect(() => {
    if (!user || !novel?.novelId || viewedNovelRef.current === String(novel.novelId)) {
      return;
    }

    viewedNovelRef.current = String(novel.novelId);
    recordNovelViewMutation.mutate();
  }, [novel?.novelId, recordNovelViewMutation, user]);

  async function handleUpdate(payload: NovelPayload) {
    await updateNovelMutation.mutateAsync(payload);
    setIsEditing(false);
  }

  function scheduleDeleteNovel() {
    if (!novelId || deleteCountdown !== null) {
      return;
    }

    setDeleteCountdown(5);

    deleteIntervalRef.current = setInterval(() => {
      setDeleteCountdown((current) => {
        if (!current || current <= 1) {
          return current;
        }

        return current - 1;
      });
    }, 1000);

    deleteTimeoutRef.current = setTimeout(async () => {
      if (deleteIntervalRef.current) {
        clearInterval(deleteIntervalRef.current);
        deleteIntervalRef.current = null;
      }
      deleteTimeoutRef.current = null;
      setDeleteCountdown(null);

      await deleteNovelMutation.mutateAsync();
      navigate(routes.novels);
    }, 5000);
  }

  function undoDeleteNovel() {
    if (deleteIntervalRef.current) {
      clearInterval(deleteIntervalRef.current);
      deleteIntervalRef.current = null;
    }
    if (deleteTimeoutRef.current) {
      clearTimeout(deleteTimeoutRef.current);
      deleteTimeoutRef.current = null;
    }

    setDeleteCountdown(null);
  }

  async function refetchCrawledNovel(sourceUrl: string | null) {
    if (!sourceUrl) {
      toast.error(t('novelPages.crawledSourceMissing'));
      return;
    }

    await createCrawlTaskMutation.mutateAsync({ url: sourceUrl });
  }

  if (isLoading) {
    return (
      <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
        <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">
          {t('novelPages.loading')}
        </div>
      </main>
    );
  }

  if (isError || !novel) {
    return (
      <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
        <Card>
          <CardContent className="grid gap-4 p-6">
            <p className="font-semibold">{t('novelPages.notFound')}</p>
            <Button asChild className="w-fit" variant="outline">
              <Link to={routes.novels}>{t('novelPages.backToLibrary')}</Link>
            </Button>
          </CardContent>
        </Card>
      </main>
    );
  }

  if (isEditing) {
    return (
      <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
        <div className="flex items-center justify-between gap-3">
          <Button asChild variant="ghost">
            <Link to={routes.novels}>
              <ArrowLeft />
              {t('novelPages.library')}
            </Link>
          </Button>
        </div>
        <NovelForm
          mode="edit"
          novel={novel}
          isSubmitting={updateNovelMutation.isPending}
          onCancel={() => setIsEditing(false)}
          onSubmit={handleUpdate}
        />
      </main>
    );
  }

  const crawledSourceUrl = extractCrawledSourceUrl(novel.description);
  const hasCrawledTag = novelTagsQuery.data?.some((tag) => tag.name.toLowerCase() === 'crawled') ?? false;
  const canRefetchCrawledNovel =
    canEdit && Boolean(crawledSourceUrl) && hasCrawledTag;
  const firstChapter = [...chapters].sort((left, right) => left.chapterNumber - right.chapterNumber)[0];

  function toggleMetricInteraction(metric: 'likes' | 'bookmarks' | 'follows') {
    if (!user) {
      navigate(routes.login);
      return;
    }

    if (metric === 'likes') {
      likeMutation.mutate();
    } else if (metric === 'bookmarks') {
      bookmarkMutation.mutate();
    } else {
      followMutation.mutate();
    }
  }

  function isMetricInteractionPending(metric: 'likes' | 'bookmarks' | 'follows') {
    return metric === 'likes'
      ? likeMutation.isPending
      : metric === 'bookmarks'
        ? bookmarkMutation.isPending
        : followMutation.isPending;
  }

  function isMetricInteractionActive(metric: 'likes' | 'bookmarks' | 'follows') {
    return metric === 'likes' ? interaction?.liked : metric === 'bookmarks' ? interaction?.bookmarked : interaction?.followed;
  }

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <div className="flex items-center justify-between gap-3">
        <Button asChild variant="ghost">
            <Link to={routes.novels}>
              <ArrowLeft />
              {t('novelPages.library')}
            </Link>
        </Button>
        {canEdit ? (
          <div className="flex flex-wrap gap-2">
            <Button aria-label={t('novelPages.newChapter')} asChild size="icon" title={t('novelPages.newChapter')} variant="outline">
              <Link to={`/novels/${novel.novelId}/chapters/new`}>
                <Plus />
              </Link>
            </Button>
            {canRefetchCrawledNovel ? (
              <Button
                aria-label={t('novelPages.refetchCrawled')}
                disabled={createCrawlTaskMutation.isPending}
                size="icon"
                title={t('novelPages.refetchCrawled')}
                type="button"
                variant="outline"
                onClick={() => refetchCrawledNovel(crawledSourceUrl)}
              >
                <RefreshCw className={createCrawlTaskMutation.isPending ? 'animate-spin' : undefined} />
              </Button>
            ) : null}
            <Button aria-label={t('novelPages.editNovel')} size="icon" title={t('novelPages.editNovel')} type="button" variant="outline" onClick={() => setIsEditing(true)}>
              <Pencil />
            </Button>
            {deleteCountdown === null ? (
              <Button
                aria-label={t('novelPages.deleteNovel')}
                disabled={deleteNovelMutation.isPending}
                size="icon"
                title={t('novelPages.deleteNovel')}
                type="button"
                variant="destructive"
                onClick={scheduleDeleteNovel}
              >
                <Trash2 />
              </Button>
            ) : (
              <Button
                disabled={deleteNovelMutation.isPending}
                type="button"
                variant="outline"
                onClick={undoDeleteNovel}
              >
                <RotateCcw />
                <span className="font-bold">{deleteCountdown}s</span>
                {t('novelPages.undoDelete')}
              </Button>
            )}
          </div>
        ) : null}
      </div>

      <section className="grid gap-6 lg:grid-cols-[320px_minmax(0,1fr)]">
        <div className="grid content-start gap-4">
          <NovelCover src={novel.coverImageUrl} title={novel.title} />
          {firstChapter ? (
            <Button asChild>
              <Link to={`/novels/${novel.novelId}/chapters/${firstChapter.chapterId}`}>
                <BookOpen />
                {t('novelPages.startReading')}
              </Link>
            </Button>
          ) : (
            <Button disabled>
              <BookOpen />
              {t('novelPages.startReading')}
            </Button>
          )}
          {novel.latestChapterId ? (
            <Button asChild variant="outline">
              <Link to={`/novels/${novel.novelId}/chapters/${novel.latestChapterId}`}>
                <BookOpen />
                {t('novelPages.readLatest')}
              </Link>
            </Button>
          ) : null}
          {canEdit ? (
            <div className="grid gap-2">
              <Button
                aria-expanded={isTagsOpen}
                aria-label={t(isTagsOpen ? 'novelPages.hideTags' : 'novelPages.showTags')}
                className="w-fit"
                title={t(isTagsOpen ? 'novelPages.hideTags' : 'novelPages.showTags')}
                type="button"
                variant="outline"
                onClick={() => setIsTagsOpen((current) => !current)}
              >
                <ChevronDown className={isTagsOpen ? 'rotate-180 transition-transform' : 'transition-transform'} />
                {t(isTagsOpen ? 'novelPages.hideTags' : 'novelPages.showTags')}
              </Button>
              {isTagsOpen ? (
                <div className="grid gap-2 rounded-lg border bg-muted/40 p-3">
                  <p className="text-sm font-bold">{t('novelPages.tags')}</p>
                  {novelTagsQuery.isLoading ? (
                    <p className="text-sm text-muted-foreground">{t('novelPages.loadingTags')}</p>
                  ) : novelTagsQuery.data?.length ? (
                    <div className="flex flex-wrap gap-2">
                      {novelTagsQuery.data.map((tag) => (
                        <Badge key={tag.tagId} variant="secondary">
                          {tag.name}
                        </Badge>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-muted-foreground">{t('novelPages.noTags')}</p>
                  )}
                </div>
              ) : null}
            </div>
          ) : null}
        </div>

        <div className="grid gap-5">
          <div className="grid gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <Badge>{t(`novelForm.statusOptions.${novel.status}`)}</Badge>
              {novel.genres.map((genre) => (
                <Badge key={genre} variant="secondary">
                  {genre}
                </Badge>
              ))}
            </div>
            <h1 className="text-4xl font-extrabold leading-tight tracking-normal md:text-6xl">
              {novel.title}
              {novel.accessStatus === 'PREVIEW_ONLY' ? (
                <sup className="ml-2 inline-flex translate-y-[-0.45em] rounded-full border border-yellow-300/70 bg-gradient-to-b from-yellow-300 to-amber-500 px-2 py-0.5 align-baseline text-xs font-extrabold uppercase tracking-normal text-white shadow-sm shadow-amber-900/20 ring-1 ring-amber-100/60 md:text-sm">
                  {t('novelForm.accessStatusOptions.PREVIEW_ONLY')}
                </sup>
              ) : null}
            </h1>
            <p className="text-lg font-semibold text-muted-foreground">{novel.author}</p>
            <p className="text-sm font-semibold text-muted-foreground">
              {t('novelPages.chapterCount', { count: novel.chapterCount ?? 0 })}
            </p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            {metricItems.map((item) => {
              const isInteractive = item.key !== 'views';
              const isPending = isInteractive && isMetricInteractionPending(item.key);
              const isActive = isInteractive && isMetricInteractionActive(item.key);

              return (
                <Card key={item.key} className={isActive ? 'border-primary/70 bg-primary/20 dark:border-primary/80 dark:bg-primary/30' : undefined}>
                  {isInteractive ? (
                    <button
                      aria-label={t(item.labelKey)}
                      aria-pressed={isActive}
                      className="flex w-full items-center gap-3 p-4 text-left transition hover:bg-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-60"
                      disabled={isPending}
                      type="button"
                      onClick={() => toggleMetricInteraction(item.key)}
                    >
                      <item.icon className="size-5 text-primary" fill={isActive ? 'currentColor' : 'none'} />
                      <div>
                        <p className="text-lg font-bold">{formatCount(interaction?.[item.key] ?? novel[item.key])}</p>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">{t(item.labelKey)}</p>
                      </div>
                    </button>
                  ) : (
                    <CardContent className="flex items-center gap-3 p-4">
                      <item.icon className="size-5 text-primary" />
                      <div>
                        <p className="text-lg font-bold">{formatCount(interaction?.[item.key] ?? novel[item.key])}</p>
                        <p className="text-xs font-semibold uppercase text-muted-foreground">{t(item.labelKey)}</p>
                      </div>
                    </CardContent>
                  )}
                </Card>
              );
            })}
          </div>

          <Card>
            <CardHeader>
              <CardTitle>{t('novelPages.description')}</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="whitespace-pre-line leading-7 text-muted-foreground">{novel.description}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>{t('novelPages.publication')}</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 text-sm text-muted-foreground sm:grid-cols-2">
              <p>
                <span className="font-semibold text-foreground">{t('novelPages.created')}</span>{' '}
                {formatDateTime(novel.createAt)}
              </p>
              <p>
                <span className="font-semibold text-foreground">{t('novelPages.updated')}</span>{' '}
                {formatDateTime(novel.updateAt)}
              </p>
            </CardContent>
          </Card>

          <ChapterListSection novelId={String(novel.novelId)} />
        </div>
      </section>
      <CommentSection isAnchorReady={!isChaptersLoading} target={{ id: String(novel.novelId), type: 'novel' }} />
      <SimilarNovelsSection novelId={String(novel.novelId)} />
    </main>
  );
}
