import { useEffect, useMemo } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { ImageUploader } from '@/features/uploads/components/image-uploader';
import { useUploadNovelCoverMutation } from '@/features/uploads/hooks/use-image-upload';
import { cn } from '@/lib/utils';
import { NovelCover } from './novel-cover';
import { useGenres, useNovelTags, useTags } from '../hooks/use-novels';
import type { Novel, NovelPayload, NovelStatus } from '../types';

const novelFormSchema = z.object({
  title: z.string().min(2),
  author: z.string().min(2),
  description: z.string().min(10),
  coverImageUrl: z.string().url().or(z.literal('')),
  status: z.enum(['ONGOING', 'COMPLETED']),
  genres: z.array(z.string()),
  tags: z.array(z.string()),
});

type NovelFormValues = z.infer<typeof novelFormSchema>;

type NovelFormProps = {
  novel?: Novel;
  mode: 'create' | 'edit';
  isSubmitting?: boolean;
  onCancel?: () => void;
  onSubmit: (payload: NovelPayload) => void;
};

const statusOptions: NovelStatus[] = ['ONGOING', 'COMPLETED'];

function toFormValues(novel?: Novel, tagNames: string[] = []): NovelFormValues {
  return {
    title: novel?.title ?? '',
    author: novel?.author ?? '',
    description: novel?.description ?? '',
    coverImageUrl: novel?.coverImageUrl ?? '',
    status: novel?.status ?? 'ONGOING',
    genres: novel?.genres ?? [],
    tags: tagNames,
  };
}

export function NovelForm({ novel, mode, isSubmitting = false, onCancel, onSubmit }: NovelFormProps) {
  const { t } = useTranslation();
  const { data: genres = [], isLoading: isLoadingGenres } = useGenres();
  const { data: tags = [], isLoading: isLoadingTags } = useTags();
  const { data: novelTags } = useNovelTags(novel?.novelId ? String(novel.novelId) : undefined);
  const novelTagNames = useMemo(() => novelTags?.map((tag) => tag.name) ?? [], [novelTags]);
  const uploadCoverMutation = useUploadNovelCoverMutation(String(novel?.novelId ?? ''));
  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
    setValue,
    watch,
  } = useForm<NovelFormValues>({
    resolver: zodResolver(novelFormSchema),
    defaultValues: toFormValues(novel),
  });
  const selectedGenres = watch('genres');
  const selectedTags = watch('tags');
  const coverImageUrl = watch('coverImageUrl');
  const title = watch('title');

  useEffect(() => {
    reset(toFormValues(novel, novelTagNames));
  }, [novel, novelTagNames, reset]);

  function toggleGenre(genreName: string) {
    const nextGenres = selectedGenres.includes(genreName)
      ? selectedGenres.filter((genre) => genre !== genreName)
      : [...selectedGenres, genreName];

    setValue('genres', nextGenres, { shouldDirty: true, shouldValidate: true });
  }

  function toggleTag(tagName: string) {
    const nextTags = selectedTags.includes(tagName)
      ? selectedTags.filter((tag) => tag !== tagName)
      : [...selectedTags, tagName];

    setValue('tags', nextTags, { shouldDirty: true, shouldValidate: true });
  }

  async function handleCoverUpload(file: File) {
    if (!novel?.novelId) {
      return;
    }

    const updatedNovel = await uploadCoverMutation.mutateAsync(file);
    setValue('coverImageUrl', updatedNovel.coverImageUrl ?? '', {
      shouldDirty: true,
      shouldValidate: true,
    });
  }

  return (
    <form
      className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_340px]"
      onSubmit={handleSubmit((values) => onSubmit(values))}
    >
      <Card>
        <CardHeader>
          <CardTitle>
            {mode === 'create' ? t('novelForm.storyInformation') : t('novelForm.editStoryInformation')}
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4">
          <label className="grid gap-2 text-sm font-semibold">
            {t('novelForm.title')}
            <Input placeholder={t('novelForm.titlePlaceholder')} {...register('title')} />
            {errors.title ? (
              <span className="text-xs text-destructive">{t('novelForm.titleError')}</span>
            ) : null}
          </label>

          <label className="grid gap-2 text-sm font-semibold">
            {t('novelForm.author')}
            <Input placeholder={t('novelForm.authorPlaceholder')} {...register('author')} />
            {errors.author ? (
              <span className="text-xs text-destructive">{t('novelForm.authorError')}</span>
            ) : null}
          </label>

          <label className="grid gap-2 text-sm font-semibold">
            {t('novelForm.description')}
            <textarea
              className="min-h-44 rounded-md border bg-background px-3 py-2 text-sm text-foreground shadow-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
              placeholder={t('novelForm.descriptionPlaceholder')}
              {...register('description')}
            />
            {errors.description ? (
              <span className="text-xs text-destructive">{t('novelForm.descriptionError')}</span>
            ) : null}
          </label>

          <div className="grid gap-2">
            <p className="text-sm font-semibold">{t('novelForm.genres')}</p>
            {isLoadingGenres ? (
              <p className="text-sm text-muted-foreground">{t('novelForm.loadingGenres')}</p>
            ) : (
              <div className="flex flex-wrap gap-2">
                {genres.map((genre) => {
                  const isSelected = selectedGenres.includes(genre.name);

                  return (
                    <button
                      className={cn(
                        'rounded-full border px-3 py-1 text-sm font-semibold transition-colors hover:border-primary',
                        isSelected && 'border-primary bg-primary text-primary-foreground',
                      )}
                      key={genre.genreId}
                      type="button"
                      onClick={() => toggleGenre(genre.name)}
                    >
                      {genre.name}
                    </button>
                  );
                })}
              </div>
            )}
            {selectedGenres.length ? (
              <div className="flex flex-wrap gap-2 pt-1">
                {selectedGenres.map((genre) => (
                  <Badge key={genre} variant="secondary">
                    {genre}
                  </Badge>
                ))}
              </div>
            ) : null}
          </div>

          <div className="grid gap-2">
            <p className="text-sm font-semibold">{t('novelForm.tags')}</p>
            {isLoadingTags ? (
              <p className="text-sm text-muted-foreground">{t('novelForm.loadingTags')}</p>
            ) : (
              <div className="flex flex-wrap gap-2">
                {tags.map((tag) => {
                  const isSelected = selectedTags.includes(tag.name);

                  return (
                    <button
                      className={cn(
                        'rounded-full border px-3 py-1 text-sm font-semibold transition-colors hover:border-primary',
                        isSelected && 'border-primary bg-primary text-primary-foreground',
                      )}
                      key={tag.tagId}
                      type="button"
                      onClick={() => toggleTag(tag.name)}
                    >
                      {tag.name}
                    </button>
                  );
                })}
              </div>
            )}
            {selectedTags.length ? (
              <div className="flex flex-wrap gap-2 pt-1">
                {selectedTags.map((tag) => (
                  <Badge key={tag} variant="secondary">
                    {tag}
                  </Badge>
                ))}
              </div>
            ) : null}
          </div>
        </CardContent>
      </Card>

      <div className="grid content-start gap-4">
        <Card>
          <CardHeader>
            <CardTitle>{t('novelForm.coverAndStatus')}</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4">
            {novel?.novelId ? (
              <ImageUploader
                buttonLabel={t('novelForm.uploadCover')}
                currentImageUrl={coverImageUrl}
                description={t('novelForm.uploadCoverDescription')}
                isUploading={uploadCoverMutation.isPending}
                title={t('novelForm.cloudinaryCover')}
                onUpload={handleCoverUpload}
              />
            ) : (
              <>
                <NovelCover src={coverImageUrl} title={title || t('novelForm.coverPreview')} />
                <p className="rounded-md border bg-muted px-3 py-2 text-xs leading-5 text-muted-foreground">
                  {t('novelForm.uploadAfterCreate')}
                </p>
              </>
            )}
            <label className="grid gap-2 text-sm font-semibold">
              {t('novelForm.coverImageUrl')}
              <Input placeholder="https://..." {...register('coverImageUrl')} />
              {errors.coverImageUrl ? (
                <span className="text-xs text-destructive">{t('novelForm.coverUrlError')}</span>
              ) : null}
            </label>
            <label className="grid gap-2 text-sm font-semibold">
              {t('novelForm.status')}
              <select
                className="h-10 rounded-md border bg-background px-3 text-sm text-foreground shadow-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                {...register('status')}
              >
                {statusOptions.map((status) => (
                  <option key={status} value={status}>
                    {t(`novelForm.statusOptions.${status}`)}
                  </option>
                ))}
              </select>
            </label>
          </CardContent>
        </Card>

        <div className="flex flex-col gap-2 sm:flex-row lg:flex-col">
          <Button disabled={isSubmitting} type="submit">
            {isSubmitting
              ? t('novelForm.saving')
              : mode === 'create'
                ? t('novelForm.createNovel')
                : t('novelForm.saveChanges')}
          </Button>
          {onCancel ? (
            <Button disabled={isSubmitting} type="button" variant="outline" onClick={onCancel}>
              {t('novelForm.cancel')}
            </Button>
          ) : null}
        </div>
      </div>
    </form>
  );
}
