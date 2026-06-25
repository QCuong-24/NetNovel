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

  return (
    <Card className="h-full min-w-0 overflow-hidden">
      <CardContent className="grid h-full grid-rows-[auto_minmax(0,1fr)_auto] gap-3 p-2.5 sm:gap-4 sm:p-3">
        <div className="grid gap-2 sm:gap-3">
          <Link to={`/novels/${novel.novelId}`}>
            <NovelCover className="rounded-md" src={novel.coverImageUrl} title={novel.title} />
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
        </div>
        <div className="grid content-start gap-2">
          <div className="flex flex-wrap gap-1">
            <Badge variant="secondary">{novel.status}</Badge>
            {novel.genres.slice(0, 2).map((genre) => (
              <Badge key={genre} variant="outline">
                {genre}
              </Badge>
            ))}
          </div>
          <Link className="line-clamp-2 text-sm font-bold hover:text-primary hover:underline" to={`/novels/${novel.novelId}`}>
            {novel.title}
          </Link>
          <p className="line-clamp-1 text-xs font-semibold text-muted-foreground sm:text-sm">{novel.author}</p>
          <p className="hidden text-sm leading-6 text-muted-foreground sm:line-clamp-2">{novel.description}</p>
        </div>
        <div className="mt-auto">
          {novel.latestChapterId && novel.latestChapterNumber ? (
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
