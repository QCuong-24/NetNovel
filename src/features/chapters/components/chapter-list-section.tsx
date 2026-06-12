import { Pencil, Plus } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { useNovelChapters } from '../hooks/use-chapters';

type ChapterListSectionProps = {
  novelId: string;
};

export function ChapterListSection({ novelId }: ChapterListSectionProps) {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const { data: chapters = [], isLoading } = useNovelChapters(novelId);
  const canEditChapter = canManageNovels(user);

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
        {chapters.map((chapter) => (
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
                {t('chapters.updated')} {formatDateTime(chapter.updateAt)}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              {canEditChapter ? (
                <Button aria-label={t('chapters.edit')} asChild size="icon" variant="outline">
                  <Link to={`/novels/${chapter.novelId}/chapters/${chapter.chapterId}/edit`}>
                    <Pencil />
                  </Link>
                </Button>
              ) : null}
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
