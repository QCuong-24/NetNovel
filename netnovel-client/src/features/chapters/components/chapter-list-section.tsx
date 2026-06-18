import { Pencil, Plus, RotateCcw, Trash2 } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { useDeleteChapterMutation, useNovelChapters } from '../hooks/use-chapters';

type ChapterListSectionProps = {
  novelId: string;
};

function removeCountdown(countdowns: Record<number, number>, chapterId: number) {
  const nextCountdowns = { ...countdowns };
  delete nextCountdowns[chapterId];

  return nextCountdowns;
}

export function ChapterListSection({ novelId }: ChapterListSectionProps) {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const { data: chapters = [], isLoading } = useNovelChapters(novelId);
  const deleteChapterMutation = useDeleteChapterMutation(novelId);
  const canEditChapter = canManageNovels(user);
  const [showAllChapters, setShowAllChapters] = useState(false);
  const [pendingDeleteCountdowns, setPendingDeleteCountdowns] = useState<Record<number, number>>({});
  const deleteTimeoutsRef = useRef(new Map<number, ReturnType<typeof setTimeout>>());
  const deleteIntervalsRef = useRef(new Map<number, ReturnType<typeof setInterval>>());
  const visibleChapters = showAllChapters ? chapters : chapters.slice(0, 10);
  const hasHiddenChapters = chapters.length > visibleChapters.length;

  useEffect(() => {
    const timeouts = deleteTimeoutsRef.current;
    const intervals = deleteIntervalsRef.current;

    return () => {
      timeouts.forEach((timer) => clearTimeout(timer));
      intervals.forEach((timer) => clearInterval(timer));
      timeouts.clear();
      intervals.clear();
    };
  }, []);

  function scheduleDeleteChapter(chapterId: number) {
    if (deleteTimeoutsRef.current.has(chapterId)) {
      return;
    }

    setPendingDeleteCountdowns((current) => ({ ...current, [chapterId]: 5 }));

    const interval = setInterval(() => {
      setPendingDeleteCountdowns((current) => {
        const currentSeconds = current[chapterId];

        if (!currentSeconds || currentSeconds <= 1) {
          return current;
        }

        return { ...current, [chapterId]: currentSeconds - 1 };
      });
    }, 1000);

    const timeout = setTimeout(() => {
      clearInterval(interval);
      deleteIntervalsRef.current.delete(chapterId);
      deleteTimeoutsRef.current.delete(chapterId);
      setPendingDeleteCountdowns((current) => removeCountdown(current, chapterId));
      deleteChapterMutation.mutate(String(chapterId));
    }, 5000);

    deleteIntervalsRef.current.set(chapterId, interval);
    deleteTimeoutsRef.current.set(chapterId, timeout);
  }

  function undoDeleteChapter(chapterId: number) {
    const timeout = deleteTimeoutsRef.current.get(chapterId);
    const interval = deleteIntervalsRef.current.get(chapterId);

    if (timeout) {
      clearTimeout(timeout);
      deleteTimeoutsRef.current.delete(chapterId);
    }

    if (interval) {
      clearInterval(interval);
      deleteIntervalsRef.current.delete(chapterId);
    }

    setPendingDeleteCountdowns((current) => removeCountdown(current, chapterId));
  }

  return (
    <Card>
      <CardHeader className="flex w-full flex-row items-center justify-between gap-3">
        <CardTitle>{t('chapters.title')}</CardTitle>
        {canEditChapter ? (
          <Button aria-label={t('chapters.newChapter')} asChild className="ml-auto" size="icon" variant="outline">
            <Link to={`/novels/${novelId}/chapters/new`}>
              <Plus />
            </Link>
          </Button>
        ) : null}
      </CardHeader>
      <CardContent className="grid gap-2">
        {isLoading ? <p className="text-sm text-muted-foreground">{t('chapters.loading')}</p> : null}
        {!isLoading && !chapters.length ? (
          <p className="text-sm text-muted-foreground">{t('chapters.empty')}</p>
        ) : null}
        {visibleChapters.map((chapter) => {
          const pendingDeleteSeconds = pendingDeleteCountdowns[chapter.chapterId];
          const isPendingDelete = typeof pendingDeleteSeconds === 'number';

          return (
            <div
              className="grid gap-3 rounded-lg border p-3 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-center"
              key={chapter.chapterId}
            >
              <div className="min-w-0">
                <Link
                  className="font-semibold hover:text-primary hover:underline"
                  to={`/novels/${chapter.novelId}/chapters/${chapter.chapterId}`}
                >
                  {t('chapters.chapter')} {chapter.chapterNumber}: {chapter.title}
                </Link>
                <p className="mt-1 text-xs text-muted-foreground">
                  {isPendingDelete
                    ? t('chapters.deletePending', { seconds: pendingDeleteSeconds })
                    : `${t('chapters.updated')} ${formatDateTime(chapter.updateAt)}`}
                </p>
              </div>
              <div className="flex flex-wrap gap-2">
                {canEditChapter ? (
                  <>
                    {isPendingDelete ? (
                      <Button
                        aria-label={t('chapters.undoDelete')}
                        disabled={deleteChapterMutation.isPending}
                        size="icon"
                        type="button"
                        variant="outline"
                        onClick={() => undoDeleteChapter(chapter.chapterId)}
                      >
                        <RotateCcw />
                      </Button>
                    ) : (
                      <>
                        <Button aria-label={t('chapters.edit')} asChild size="icon" variant="outline">
                          <Link to={`/novels/${chapter.novelId}/chapters/${chapter.chapterId}/edit`}>
                            <Pencil />
                          </Link>
                        </Button>
                        <Button
                          aria-label={t('chapters.delete')}
                          disabled={deleteChapterMutation.isPending}
                          size="icon"
                          type="button"
                          variant="destructive"
                          onClick={() => scheduleDeleteChapter(chapter.chapterId)}
                        >
                          <Trash2 />
                        </Button>
                      </>
                    )}
                  </>
                ) : null}
              </div>
            </div>
          );
        })}
        {chapters.length > 10 ? (
          <Button
            className="mt-2 w-full"
            type="button"
            variant="outline"
            onClick={() => setShowAllChapters((current) => !current)}
          >
            {hasHiddenChapters
              ? t('chapters.viewMore', { count: chapters.length - visibleChapters.length })
              : t('chapters.viewLess')}
          </Button>
        ) : null}
      </CardContent>
    </Card>
  );
}
