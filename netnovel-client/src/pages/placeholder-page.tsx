import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

type PlaceholderPageProps = {
  titleKey: string;
};

export function PlaceholderPage({ titleKey }: PlaceholderPageProps) {
  const { t } = useTranslation();

  return (
    <Card className="mx-auto mt-8 w-full max-w-3xl">
      <CardHeader>
        <CardTitle>{t(titleKey)}</CardTitle>
      </CardHeader>
      <CardContent>
        <p className="text-muted-foreground">{t('common.foundationReady')}</p>
      </CardContent>
    </Card>
  );
}
