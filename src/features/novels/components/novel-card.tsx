import { BookOpen, Eye, Heart, Users } from 'lucide-react';
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
    <Card className="h-full overflow-hidden">
      <CardContent className="grid h-full grid-rows-[auto_minmax(0,1fr)_auto] gap-4 p-3">
        <div className="grid gap-3">
          <Link to={`/novels/${novel.novelId}`}>
            <NovelCover className="rounded-md" src={novel.coverImageUrl} title={novel.title} />
          </Link>
          <div className="grid grid-cols-3 gap-2 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <Eye className="size-3.5" />
              {formatCount(novel.views)}
            </span>
            <span className="flex items-center gap-1">
              <Users className="size-3.5" />
              {formatCount(novel.follows)}
            </span>
            <span className="flex items-center gap-1">
              <Heart className="size-3.5" />
              {formatCount(novel.likes)}
            </span>
          </div>
        </div>
        <div className="grid content-start gap-2">
          <div className="flex flex-wrap gap-1">
            <Badge variant="secondary">{novel.status}</Badge>
            {novel.tags.slice(0, 2).map((tag) => (
              <Badge key={tag} variant="outline">
                {tag}
              </Badge>
            ))}
          </div>
          <Link className="line-clamp-2 text-base font-bold hover:text-primary hover:underline" to={`/novels/${novel.novelId}`}>
            {novel.title}
          </Link>
          <p className="line-clamp-1 text-sm font-semibold text-muted-foreground">{novel.author}</p>
          <p className="line-clamp-3 text-sm leading-6 text-muted-foreground">{novel.description}</p>
        </div>
        <div className="mt-auto">
          {novel.latestChapterId && novel.latestChapterNumber ? (
            <Link
              className="inline-flex w-full items-center gap-2 border-t pt-3 text-sm font-bold text-primary hover:underline"
              to={`/novels/${novel.novelId}/chapters/${novel.latestChapterId}`}
            >
              <BookOpen className="size-4" />
              {t('novelPages.latestChapter', { number: novel.latestChapterNumber })}
            </Link>
          ) : (
            <span className="block border-t pt-3 text-sm font-semibold text-muted-foreground">
              {t('novelPages.noChapters')}
            </span>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
