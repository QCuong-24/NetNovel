import { ArrowLeft, BookOpen, Eye, Heart, Pencil, Plus, Users } from 'lucide-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router-dom';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { routes } from '@/config/routes';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { ChapterListSection } from '@/features/chapters/components/chapter-list-section';
import { NovelCover } from '../components/novel-cover';
import { NovelForm } from '../components/novel-form';
import { formatCount, formatDateTime } from '../lib/novel-format';
import { canManageNovels } from '../lib/novel-permissions';
import { useNovel, useUpdateNovelMutation } from '../hooks/use-novels';
import type { NovelPayload } from '../types';

const metricItems = [
  { key: 'views', labelKey: 'novelPages.metrics.views', icon: Eye },
  { key: 'follows', labelKey: 'novelPages.metrics.follows', icon: Users },
  { key: 'likes', labelKey: 'novelPages.metrics.likes', icon: Heart },
] as const;

export function NovelDetailPage() {
  const { t } = useTranslation();
  const { novelId } = useParams();
  const [isEditing, setIsEditing] = useState(false);
  const { data: user } = useCurrentUser();
  const canEdit = canManageNovels(user);
  const { data: novel, isError, isLoading } = useNovel(novelId);
  const updateNovelMutation = useUpdateNovelMutation(novelId ?? '');

  async function handleUpdate(payload: NovelPayload) {
    await updateNovelMutation.mutateAsync(payload);
    setIsEditing(false);
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
            <Button asChild variant="outline">
              <Link to={`/novels/${novel.novelId}/chapters/new`}>
                <Plus />
                {t('novelPages.newChapter')}
              </Link>
            </Button>
            <Button type="button" variant="outline" onClick={() => setIsEditing(true)}>
              <Pencil />
              {t('novelPages.editNovel')}
            </Button>
          </div>
        ) : null}
      </div>

      <section className="grid gap-6 lg:grid-cols-[320px_minmax(0,1fr)]">
        <div className="grid content-start gap-4">
          <NovelCover src={novel.coverImageUrl} title={novel.title} />
          <Button asChild>
            <Link to={`/novels/${novel.novelId}/chapters/1`}>
              <BookOpen />
              {t('novelPages.startReading')}
            </Link>
          </Button>
        </div>

        <div className="grid gap-5">
          <div className="grid gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <Badge>{t(`novelForm.statusOptions.${novel.status}`)}</Badge>
              {novel.tags.map((tag) => (
                <Badge key={tag} variant="secondary">
                  {tag}
                </Badge>
              ))}
            </div>
            <h1 className="text-4xl font-extrabold leading-tight tracking-normal md:text-6xl">
              {novel.title}
            </h1>
            <p className="text-lg font-semibold text-muted-foreground">{novel.author}</p>
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            {metricItems.map((item) => (
              <Card key={item.key}>
                <CardContent className="flex items-center gap-3 p-4">
                  <item.icon className="size-5 text-primary" />
                  <div>
                    <p className="text-lg font-bold">{formatCount(novel[item.key])}</p>
                    <p className="text-xs font-semibold uppercase text-muted-foreground">
                      {t(item.labelKey)}
                    </p>
                  </div>
                </CardContent>
              </Card>
            ))}
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
    </main>
  );
}
