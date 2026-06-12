import { ArrowLeft, LogOut, Pencil, Settings, UserRound } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { ThemeToggle } from '@/components/layout/theme-toggle';
import { LanguageSwitcher } from '@/components/layout/language-switcher';
import { useCurrentUser, useLogoutMutation } from '@/features/auth/hooks/use-auth';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import { ReaderSettingsPanel } from './reader-settings-panel';
import type { ReaderSettings } from '../types';

type ReaderToolbarProps = {
  backTo: string;
  editTo?: string;
  settings: ReaderSettings;
  onChange: <TKey extends keyof ReaderSettings>(key: TKey, value: ReaderSettings[TKey]) => void;
};

export function ReaderToolbar({ backTo, editTo, settings, onChange }: ReaderToolbarProps) {
  const [isOpen, setIsOpen] = useState(false);
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const logoutMutation = useLogoutMutation();
  const canEditChapter = canManageNovels(user);

  return (
    <header className="sticky top-0 z-20 border-b bg-background/90 backdrop-blur">
      <div className="mx-auto flex min-h-14 max-w-5xl items-center gap-2 px-4">
        <Button asChild className="shrink-0" size="sm" variant="ghost">
          <Link to={backTo}>
            <ArrowLeft />
            <span className="hidden sm:inline">{t('common.back')}</span>
          </Link>
        </Button>
        <div className="ml-auto flex items-center gap-1">
          <Button
            aria-label={t('reader.settings')}
            type="button"
            size="sm"
            variant="outline"
            onClick={() => setIsOpen((current) => !current)}
          >
            <Settings />
            <span className="hidden sm:inline">{t('reader.settings')}</span>
          </Button>
          <ThemeToggle />
          <LanguageSwitcher />
          {canEditChapter && editTo ? (
            <Button asChild size="sm" variant="outline">
              <Link to={editTo}>
                <Pencil />
                <span className="hidden sm:inline">Edit</span>
              </Link>
            </Button>
          ) : null}
          {user ? (
            <>
              <Button asChild className="hidden md:inline-flex" size="sm" variant="ghost">
                <Link to="/profile">
                  <UserRound />
                  {user.username}
                </Link>
              </Button>
              <Button
                aria-label="Logout"
                disabled={logoutMutation.isPending}
                size="icon"
                type="button"
                variant="ghost"
                onClick={() => logoutMutation.mutate()}
              >
                <LogOut />
              </Button>
            </>
          ) : (
            <Button asChild className="hidden sm:inline-flex" size="sm" variant="outline">
              <Link to="/login">{t('auth.login')}</Link>
            </Button>
          )}
        </div>
      </div>
      {isOpen ? (
        <div className="mx-auto max-w-5xl px-4 pb-4">
          <ReaderSettingsPanel settings={settings} onChange={onChange} />
        </div>
      ) : null}
    </header>
  );
}
