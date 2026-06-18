import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';

export function NotFoundPage() {
  const { t } = useTranslation();

  return (
    <main className="grid min-h-screen place-items-center bg-background px-4 text-foreground">
      <div className="grid max-w-md gap-4 text-center">
        <p className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">404</p>
        <h1 className="text-3xl font-bold">{t('common.notFound')}</h1>
        <Button asChild>
          <Link to="/">{t('common.backHome')}</Link>
        </Button>
      </div>
    </main>
  );
}
