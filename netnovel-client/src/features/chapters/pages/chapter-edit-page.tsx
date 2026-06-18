import { ArrowLeft } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import { ChapterForm } from '../components/chapter-form';
import { useChapter, useUpdateChapterMutation } from '../hooks/use-chapters';
import type { ChapterPayload } from '../types';

export function ChapterEditPage() {
  const { t } = useTranslation();
  const { chapterId = '', novelId = '' } = useParams();
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const { data: chapter, isError, isLoading } = useChapter(chapterId);
  const updateChapterMutation = useUpdateChapterMutation(chapterId);
  const canEditChapter = canManageNovels(user);
  const backToNovel = chapter?.novelId ? `/novels/${chapter.novelId}` : `/novels/${novelId}`;

  async function handleUpdate(payload: ChapterPayload) {
    const updated = await updateChapterMutation.mutateAsync(payload);
    navigate(`/novels/${updated.novelId}/chapters/${updated.chapterId}`);
  }

  if (isLoading) {
    return (
      <main className="mx-auto grid w-full max-w-6xl gap-6 px-4 py-6 md:px-6">
        <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">
          {t('chapterPages.loading')}
        </div>
      </main>
    );
  }

  if (isError || !chapter) {
    return (
      <main className="mx-auto grid w-full max-w-6xl gap-6 px-4 py-6 md:px-6">
        <Card>
          <CardContent className="grid gap-4 p-6">
            <p className="font-semibold">{t('chapterPages.notFound')}</p>
            <Button asChild className="w-fit" variant="outline">
              <Link to={backToNovel}>{t('chapterPages.backToNovel')}</Link>
            </Button>
          </CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="mx-auto grid w-full max-w-6xl gap-6 px-4 py-6 md:px-6">
      <div className="grid gap-2">
        <Button asChild className="w-fit" variant="ghost">
          <Link to={backToNovel}>
            <ArrowLeft />
            {t('chapterPages.novelPage')}
          </Link>
        </Button>
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">
            {t('chapterPages.editEyebrow')}
          </p>
          <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">
            {t('chapterPages.editTitle', { number: chapter.chapterNumber, title: chapter.title })}
          </h1>
          <p className="mt-2 max-w-2xl text-muted-foreground">{chapter.novelTitle}</p>
        </div>
      </div>

      {canEditChapter ? (
        <ChapterForm
          mode="edit"
          chapter={chapter}
          isSubmitting={updateChapterMutation.isPending}
          onCancel={() => navigate(`/novels/${chapter.novelId}/chapters/${chapter.chapterId}`)}
          onSubmit={handleUpdate}
        />
      ) : (
        <Card>
          <CardContent className="grid gap-4 p-6">
            <p className="font-semibold">{t('chapterPages.editNoPermission')}</p>
            <Button asChild className="w-fit" variant="outline">
              <Link to={backToNovel}>{t('chapterPages.backToNovel')}</Link>
            </Button>
          </CardContent>
        </Card>
      )}
    </main>
  );
}
