import { useTranslation } from 'react-i18next';
import { RankingPanel } from '../components/ranking-panel';

export function RankingPage() {
  const { t } = useTranslation();

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <header className="grid gap-2">
        <p className="text-sm font-semibold uppercase text-primary">{t('rankingPage.eyebrow')}</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">{t('rankingPage.title')}</h1>
        <p className="max-w-3xl text-sm leading-6 text-muted-foreground">{t('rankingPage.description')}</p>
      </header>

      <RankingPanel />
    </main>
  );
}
