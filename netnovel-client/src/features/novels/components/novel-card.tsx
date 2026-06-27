import { BookOpen, Bookmark, Eye, Heart, Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { NovelCover } from './novel-cover';
import { formatCount } from '../lib/novel-format';
import type { Novel } from '../types';

type NovelCardProps = {
  novel: Novel;
};

export function NovelCard({ novel }: NovelCardProps) {
  const { t } = useTranslation();
  const isPreviewOnly = novel.accessStatus === 'PREVIEW_ONLY';

  return (
    <Card className="surface-motion h-full min-w-0 overflow-hidden">
      <CardContent className="grid h-full grid-rows-[auto_minmax(0,1fr)_auto] gap-2.5 p-2 sm:gap-3 sm:p-2.5">
        <div className="grid gap-2">
          <Link to={`/novels/${novel.novelId}`}>
            <NovelCover className="aspect-[5/6] rounded-md" src={novel.coverImageUrl} title={novel.title} />
          </Link>
          <div className="grid grid-cols-2 gap-1.5 text-[11px] text-muted-foreground sm:grid-cols-4 sm:gap-2 sm:text-xs">
            <span className="flex min-w-0 items-center gap-1">
              <Eye className="size-3.5" />
              {formatCount(novel.views)}
            </span>
            <span className="flex min-w-0 items-center gap-1">
              <Heart className="size-3.5" />
              {formatCount(novel.likes)}
            </span>
            <span className="flex min-w-0 items-center gap-1">
              <Bookmark className="size-3.5" />
              {formatCount(novel.bookmarks)}
            </span>
            <span className="flex min-w-0 items-center gap-1">
              <Users className="size-3.5" />
              {formatCount(novel.follows)}
            </span>
          </div>
          <div className="flex flex-wrap gap-1">
            <Badge className="px-1.5 py-0 text-[10px] sm:text-[11px]" variant="secondary">
              {t(`novelForm.statusOptions.${novel.status}`)}
            </Badge>
            {isPreviewOnly ? (
              <Badge className="border-yellow-400/80 bg-yellow-400/20 px-1.5 py-0 text-[10px] text-yellow-700 hover:bg-yellow-400/25 dark:border-yellow-300/70 dark:bg-yellow-300/20 dark:text-yellow-200 sm:text-[11px]">
                {t('novelForm.accessStatusOptions.PREVIEW_ONLY')}
              </Badge>
            ) : null}
          </div>
        </div>
        <div className="grid content-start gap-1.5">
          <Link className="line-clamp-2 text-sm font-bold hover:text-primary hover:underline" to={`/novels/${novel.novelId}`}>
            {novel.title}
          </Link>
          <p className="line-clamp-1 text-[11px] font-semibold text-muted-foreground sm:text-xs">{novel.author}</p>
          <p className="hidden text-xs leading-5 text-muted-foreground sm:line-clamp-2">{novel.description}</p>
        </div>
        <div className="mt-auto">
          {isPreviewOnly ? (
            <Link
              className="inline-flex w-full items-center gap-1.5 border-t pt-2 text-xs font-bold text-primary hover:underline sm:gap-2 sm:pt-3 sm:text-sm"
              to={`/novels/${novel.novelId}`}
            >
              <BookOpen className="size-4" />
              <span className="line-clamp-1">{t('novelPages.readPreview')}</span>
            </Link>
          ) : novel.latestChapterId && novel.latestChapterNumber ? (
            <Link
              className="inline-flex w-full items-center gap-1.5 border-t pt-2 text-xs font-bold text-primary hover:underline sm:gap-2 sm:pt-3 sm:text-sm"
              to={`/novels/${novel.novelId}/chapters/${novel.latestChapterId}`}
            >
              <BookOpen className="size-4" />
              <span className="line-clamp-1">{t('novelPages.latestChapter', { number: novel.latestChapterNumber })}</span>
            </Link>
          ) : (
            <span className="block border-t pt-2 text-xs font-semibold text-muted-foreground sm:pt-3 sm:text-sm">
              {t('novelPages.noChapters')}
            </span>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
