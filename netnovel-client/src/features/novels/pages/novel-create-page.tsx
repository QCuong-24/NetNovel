import { ArrowLeft } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { routes } from '@/config/routes';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { NovelForm } from '../components/novel-form';
import { useCreateNovelMutation } from '../hooks/use-novels';
import { canManageNovels } from '../lib/novel-permissions';
import type { NovelPayload } from '../types';

export function NovelCreatePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const createNovelMutation = useCreateNovelMutation();
  const canCreateNovel = canManageNovels(user);

  async function handleCreate(payload: NovelPayload) {
    const novel = await createNovelMutation.mutateAsync(payload);
    navigate(`/novels/${novel.novelId}`);
  }

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <div className="grid gap-2">
        <Button asChild className="w-fit" variant="ghost">
          <Link to={routes.novels}>
            <ArrowLeft />
            {t('novelPages.library')}
          </Link>
        </Button>
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">
            {t('novelPages.createEyebrow')}
          </p>
          <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">
            {t('novelPages.createTitle')}
          </h1>
          <p className="mt-2 max-w-2xl text-muted-foreground">
            {t('novelPages.createDescription')}
          </p>
        </div>
      </div>

      {canCreateNovel ? (
        <NovelForm mode="create" isSubmitting={createNovelMutation.isPending} onSubmit={handleCreate} />
      ) : (
        <Card>
          <CardContent className="grid gap-4 p-6">
            <p className="font-semibold">{t('novelPages.createNoPermission')}</p>
            <p className="text-sm text-muted-foreground">
              {t('novelPages.createNoPermissionDescription')}
            </p>
            <Button asChild className="w-fit" variant="outline">
              <Link to={routes.novels}>{t('novelPages.backToLibrary')}</Link>
            </Button>
          </CardContent>
        </Card>
      )}
    </main>
  );
}
