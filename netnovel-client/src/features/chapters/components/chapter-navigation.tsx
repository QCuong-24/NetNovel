import { ChevronLeft, ChevronRight, List } from 'lucide-react';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import type { ChapterSummary } from '../types';

type ChapterNavigationProps = {
  chapters: ChapterSummary[];
  currentChapterId: number;
  className?: string;
};

export function ChapterNavigation({ chapters, currentChapterId, className }: ChapterNavigationProps) {
  const [isListOpen, setIsListOpen] = useState(false);
  const { t } = useTranslation();
  const sortedChapters = useMemo(
    () => [...chapters].sort((left, right) => left.chapterNumber - right.chapterNumber),
    [chapters],
  );
  const currentIndex = sortedChapters.findIndex((chapter) => chapter.chapterId === currentChapterId);
  const currentChapter = sortedChapters[currentIndex];
  const previousChapter = currentIndex > 0 ? sortedChapters[currentIndex - 1] : undefined;
  const nextChapter =
    currentIndex >= 0 && currentIndex < sortedChapters.length - 1 ? sortedChapters[currentIndex + 1] : undefined;

  if (!sortedChapters.length || !currentChapter) {
    return null;
  }

  return (
    <div className={cn('grid gap-3', className)}>
      <div className="relative grid grid-cols-[1fr_auto_1fr] items-center gap-2">
        <div className="flex justify-end">
          {previousChapter ? (
            <Button
              aria-label={t('chapters.previous')}
              asChild
              className="px-3"
              size="sm"
              variant="outline"
            >
              <Link to={`/novels/${previousChapter.novelId}/chapters/${previousChapter.chapterId}`}>
                <ChevronLeft />
                <span className="hidden sm:inline">{t('chapters.previous')}</span>
              </Link>
            </Button>
          ) : null}
        </div>

        <Button
          aria-label={t('chapters.chooseChapter')}
          className="px-3 shadow-sm"
          type="button"
          size="sm"
          variant="secondary"
          onClick={() => setIsListOpen((current) => !current)}
        >
          <List />
          <span className="hidden sm:inline">{t('chapters.title')}</span>
        </Button>

        <div className="flex justify-start">
          {nextChapter ? (
            <Button
              aria-label={t('chapters.next')}
              asChild
              className="px-3"
              size="sm"
              variant="outline"
            >
              <Link to={`/novels/${nextChapter.novelId}/chapters/${nextChapter.chapterId}`}>
                <span className="hidden sm:inline">{t('chapters.next')}</span>
                <ChevronRight />
              </Link>
            </Button>
          ) : null}
        </div>

        {isListOpen ? (
          <div className="absolute left-1/2 top-11 z-30 w-[min(92vw,420px)] -translate-x-1/2 rounded-lg border bg-card p-2 text-card-foreground shadow-xl">
            <div className="mb-2 px-2 text-xs font-bold uppercase text-muted-foreground">
              {t('chapters.chooseChapter')}
            </div>
            <div className="grid max-h-80 gap-1 overflow-y-auto overscroll-contain pr-1">
            {sortedChapters.map((chapter) => (
              <Link
                className={cn(
                  'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                  chapter.chapterId === currentChapterId && 'bg-accent text-accent-foreground',
                )}
                key={chapter.chapterId}
                to={`/novels/${chapter.novelId}/chapters/${chapter.chapterId}`}
                onClick={() => setIsListOpen(false)}
              >
                {t('chapters.chapter')} {chapter.chapterNumber}: {chapter.title}
              </Link>
            ))}
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
