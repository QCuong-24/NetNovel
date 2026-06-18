import { LibraryBig } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { routes } from '@/config/routes';
import { LanguageSwitcher } from './language-switcher';
import { ThemeToggle } from './theme-toggle';

const browseLinks = [
  { to: routes.novels, key: 'nav.library' },
  { to: routes.rankings, key: 'nav.rankings' },
  { to: routes.dashboard, key: 'nav.dashboard' },
];

const communityLinks = [
  { to: routes.notifications, key: 'nav.notifications' },
  { to: routes.profile, key: 'nav.profile' },
];

export function AppFooter() {
  const { t } = useTranslation();

  return (
    <footer className="border-t bg-card text-card-foreground">
      <div className="mx-auto grid w-full max-w-7xl gap-8 px-4 py-8 md:grid-cols-[1.4fr_1fr_1fr_1fr] md:px-6">
        <div className="grid gap-3">
          <Link to={routes.home} className="flex items-center gap-2 font-extrabold text-primary">
            <LibraryBig className="size-6" />
            <span>NetNovel</span>
          </Link>
          <p className="max-w-sm text-sm leading-6 text-muted-foreground">{t('footer.description')}</p>
        </div>

        <div className="grid content-start gap-3">
          <h2 className="text-sm font-bold">{t('footer.browse')}</h2>
          {browseLinks.map((link) => (
            <Link className="text-sm text-muted-foreground hover:text-foreground" key={link.to} to={link.to}>
              {t(link.key)}
            </Link>
          ))}
        </div>

        <div className="grid content-start gap-3">
          <h2 className="text-sm font-bold">{t('footer.community')}</h2>
          {communityLinks.map((link) => (
            <Link className="text-sm text-muted-foreground hover:text-foreground" key={link.to} to={link.to}>
              {t(link.key)}
            </Link>
          ))}
        </div>

        <div className="grid content-start gap-3">
          <h2 className="text-sm font-bold">{t('common.preferences')}</h2>
          <div className="flex flex-wrap items-center gap-2">
            <ThemeToggle />
            <LanguageSwitcher />
          </div>
        </div>
      </div>
    </footer>
  );
}
