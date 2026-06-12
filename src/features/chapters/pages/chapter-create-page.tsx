import { ArrowLeft } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { routes } from '@/config/routes';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import { useNovel } from '@/features/novels/hooks/use-novels';
import { ChapterForm } from '../components/chapter-form';
import { useCreateChapterMutation } from '../hooks/use-chapters';
import type { ChapterPayload } from '../types';

export function ChapterCreatePage() {
  const { t } = useTranslation();
  const { novelId = '' } = useParams();
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const { data: novel } = useNovel(novelId);
  const createChapterMutation = useCreateChapterMutation(novelId);
  const canCreateChapter = canManageNovels(user);

  async function handleCreate(payload: ChapterPayload) {
    const chapter = await createChapterMutation.mutateAsync(payload);
    navigate(`/novels/${novelId}/chapters/${chapter.chapterId}`);
  }

  return (
    <main className="mx-auto grid w-full max-w-6xl gap-6 px-4 py-6 md:px-6">
      <div className="grid gap-2">
        <Button asChild className="w-fit" variant="ghost">
          <Link to={`/novels/${novelId}`}>
            <ArrowLeft />
            {t('chapterPages.novelPage')}
          </Link>
        </Button>
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">
            {t('chapterPages.createEyebrow')}
          </p>
          <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">
            {novel ? novel.title : t('chapterPages.createFallbackTitle')}
          </h1>
          <p className="mt-2 max-w-2xl text-muted-foreground">
            {t('chapterPages.createDescription')}
          </p>
        </div>
      </div>

      {canCreateChapter ? (
        <ChapterForm mode="create" isSubmitting={createChapterMutation.isPending} onSubmit={handleCreate} />
      ) : (
        <Card>
          <CardContent className="grid gap-4 p-6">
            <p className="font-semibold">{t('chapterPages.createNoPermission')}</p>
            <Button asChild className="w-fit" variant="outline">
              <Link to={routes.novels}>{t('chapterPages.backToLibrary')}</Link>
            </Button>
          </CardContent>
        </Card>
      )}
    </main>
  );
}
