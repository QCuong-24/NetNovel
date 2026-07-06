import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { hasAuthTokens } from '@/features/auth/lib/auth-storage';
import { NovelCard } from './novel-card';
import { useHybridSimilarNovels, useSemanticSimilarNovels, useSimilarNovels } from '../hooks/use-novels';

type SimilarNovelsSectionProps = {
  novelId: string;
};

function canUseHybridRecommendations(roles?: string[]) {
  return Boolean(roles?.some((role) => role === 'MANAGER' || role === 'ADMIN'));
}

function translateHybridReason(t: ReturnType<typeof useTranslation>['t'], reason: string) {
  if (reason === 'Nội dung/vibe tương tự truyện gốc') {
    return t('novelPages.hybridReasons.semanticAndVibe');
  }

  if (reason === 'Có tín hiệu nội dung và metadata giống truyện gốc') {
    return t('novelPages.hybridReasons.contentMetadata');
  }

  if (reason.startsWith('Cùng genre: ')) {
    return t('novelPages.hybridReasons.sharedGenre', { value: reason.replace('Cùng genre: ', '') });
  }

  if (reason.startsWith('Có tag giống: ')) {
    return t('novelPages.hybridReasons.sharedTag', { value: reason.replace('Có tag giống: ', '') });
  }

  if (reason === 'Được nhiều người quan tâm') {
    return t('novelPages.hybridReasons.popular');
  }

  if (reason === 'Gợi ý cân bằng từ nội dung, semantic và độ phổ biến') {
    return t('novelPages.hybridReasons.balanced');
  }

  return reason;
}

export function SimilarNovelsSection({ novelId }: SimilarNovelsSectionProps) {
  const { t } = useTranslation();
  const hasTokens = hasAuthTokens();
  const currentUserQuery = useCurrentUser();
  const roles = currentUserQuery.data?.roles;
  const useHybrid = canUseHybridRecommendations(roles);
  const useSemantic = hasTokens && !currentUserQuery.isLoading && !useHybrid;
  const useBasic = !hasTokens;
  const basicQuery = useSimilarNovels(novelId, useBasic);
  const semanticQuery = useSemanticSimilarNovels(novelId, useSemantic);
  const hybridQuery = useHybridSimilarNovels(novelId, useHybrid);
  const isLoading = hasTokens && currentUserQuery.isLoading
    ? true
    : useHybrid
      ? hybridQuery.isLoading
      : useSemantic
        ? semanticQuery.isLoading
        : basicQuery.isLoading;
  const basicNovels = (useSemantic ? semanticQuery.data : basicQuery.data)?.content ?? [];
  const hybridRecommendations = hybridQuery.data?.content ?? [];
  const hasContent = useHybrid ? hybridRecommendations.length > 0 : basicNovels.length > 0;

  if (!isLoading && !hasContent) {
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
        ) : useHybrid ? (
          <div className="grid grid-cols-2 items-stretch gap-3 sm:gap-4 lg:grid-cols-3 xl:grid-cols-6">
            {hybridRecommendations.map((recommendation) => (
              <div className="grid h-full grid-rows-[1fr_auto] gap-2" key={recommendation.novel.novelId}>
                <NovelCard novel={recommendation.novel} />
                {recommendation.reasons.length ? (
                  <div className="flex flex-wrap gap-1">
                    {recommendation.reasons.slice(0, 2).map((reason) => (
                      <Badge className="text-[10px] leading-4" key={reason} variant="secondary">
                        {translateHybridReason(t, reason)}
                      </Badge>
                    ))}
                  </div>
                ) : null}
              </div>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-3 xl:grid-cols-6">
            {basicNovels.map((novel) => (
              <NovelCard key={novel.novelId} novel={novel} />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
