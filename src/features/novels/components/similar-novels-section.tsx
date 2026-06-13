import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { NovelCard } from './novel-card';
import { useSimilarNovels } from '../hooks/use-novels';

type SimilarNovelsSectionProps = {
  novelId: string;
};

export function SimilarNovelsSection({ novelId }: SimilarNovelsSectionProps) {
  const { t } = useTranslation();
  const { data, isLoading } = useSimilarNovels(novelId);
  const novels = data?.content ?? [];

  if (!isLoading && !novels.length) {
    return null;
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('novelPages.similarNovels')}</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <p className="text-sm text-muted-foreground">{t('novelPages.loadingSimilar')}</p>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5">
            {novels.map((novel) => (
              <NovelCard key={novel.novelId} novel={novel} />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
