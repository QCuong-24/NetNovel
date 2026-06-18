import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { useTranslation } from 'react-i18next';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { AutoGrowTextarea } from './auto-grow-textarea';
import type { ChapterContent, ChapterPayload } from '../types';

const chapterFormSchema = z.object({
  title: z.string().min(2),
  chapterNumber: z.number().int().positive(),
  content: z.string().min(20),
});

type ChapterFormValues = z.infer<typeof chapterFormSchema>;

type ChapterFormProps = {
  chapter?: ChapterContent;
  mode: 'create' | 'edit';
  isSubmitting?: boolean;
  onCancel?: () => void;
  onSubmit: (payload: ChapterPayload) => void;
};

function toFormValues(chapter?: ChapterContent): ChapterFormValues {
  return {
    title: chapter?.title ?? '',
    chapterNumber: chapter?.chapterNumber ?? 1,
    content: chapter?.content ?? '',
  };
}

export function ChapterForm({ chapter, mode, isSubmitting = false, onCancel, onSubmit }: ChapterFormProps) {
  const { t } = useTranslation();
  const {
    control,
    formState: { errors },
    handleSubmit,
    register,
    reset,
  } = useForm<ChapterFormValues>({
    resolver: zodResolver(chapterFormSchema),
    defaultValues: toFormValues(chapter),
  });

  useEffect(() => {
    reset(toFormValues(chapter));
  }, [chapter, reset]);

  return (
    <form className="grid gap-6" onSubmit={handleSubmit((values) => onSubmit(values))}>
      <Card>
        <CardHeader>
          <CardTitle>
            {mode === 'create' ? t('chapterForm.details') : t('chapterForm.editDetails')}
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-[minmax(0,1fr)_180px]">
          <label className="grid gap-2 text-sm font-semibold">
            {t('chapterForm.title')}
            <Input placeholder={t('chapterForm.titlePlaceholder')} {...register('title')} />
            {errors.title ? (
              <span className="text-xs text-destructive">{t('chapterForm.titleError')}</span>
            ) : null}
          </label>

          <label className="grid gap-2 text-sm font-semibold">
            {t('chapterForm.number')}
            <Input
              min={1}
              type="number"
              {...register('chapterNumber', { setValueAs: (value) => Number(value) })}
            />
            {errors.chapterNumber ? (
              <span className="text-xs text-destructive">{t('chapterForm.numberError')}</span>
            ) : null}
          </label>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t('chapterForm.content')}</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3">
          <Controller
            control={control}
            name="content"
            render={({ field }) => (
              <AutoGrowTextarea
                placeholder={t('chapterForm.contentPlaceholder')}
                {...field}
              />
            )}
          />
          {errors.content ? (
            <span className="text-xs text-destructive">{t('chapterForm.contentError')}</span>
          ) : null}
        </CardContent>
      </Card>

      <div className="flex flex-col gap-2 sm:flex-row sm:justify-end">
        {onCancel ? (
          <Button disabled={isSubmitting} type="button" variant="outline" onClick={onCancel}>
            {t('chapterForm.cancel')}
          </Button>
        ) : null}
        <Button disabled={isSubmitting} type="submit">
          {isSubmitting
            ? t('chapterForm.saving')
            : mode === 'create'
              ? t('chapterForm.createChapter')
              : t('chapterForm.saveChanges')}
        </Button>
      </div>
    </form>
  );
}
