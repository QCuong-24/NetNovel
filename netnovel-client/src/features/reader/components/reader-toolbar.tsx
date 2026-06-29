import { ArrowLeft, LogIn, Pencil, Settings, Trash2 } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { ThemeToggle } from '@/components/layout/theme-toggle';
import { LanguageSwitcher } from '@/components/layout/language-switcher';
import { UserMenu } from '@/components/layout/user-menu';
import { useCurrentUser, useLogoutMutation } from '@/features/auth/hooks/use-auth';
import { useDeleteChapterMutation } from '@/features/chapters/hooks/use-chapters';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import { ReaderSettingsPanel } from './reader-settings-panel';
import type { ReaderSettings } from '../types';

type ReaderToolbarProps = {
  backTo: string;
  editTo?: string;
  chapterId?: string;
  novelId?: string;
  settings: ReaderSettings;
  onChange: <TKey extends keyof ReaderSettings>(key: TKey, value: ReaderSettings[TKey]) => void;
};

export function ReaderToolbar({ backTo, editTo, chapterId, novelId, settings, onChange }: ReaderToolbarProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [deleteCountdown, setDeleteCountdown] = useState<number | null>(null);
  const deleteIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const deleteTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const logoutMutation = useLogoutMutation();
  const deleteChapterMutation = useDeleteChapterMutation(novelId ?? '');
  const canEditChapter = canManageNovels(user);
  const canDeleteChapter = canEditChapter && Boolean(chapterId && novelId);

  useEffect(() => {
    return () => {
      if (deleteIntervalRef.current) {
        clearInterval(deleteIntervalRef.current);
      }
      if (deleteTimeoutRef.current) {
        clearTimeout(deleteTimeoutRef.current);
      }
    };
  }, []);

  function scheduleDeleteChapter() {
    if (!chapterId || deleteCountdown !== null) {
      return;
    }

    setDeleteCountdown(5);
    deleteIntervalRef.current = setInterval(() => {
      setDeleteCountdown((current) => {
        if (!current || current <= 1) {
          return current;
        }

        return current - 1;
      });
    }, 1000);

    deleteTimeoutRef.current = setTimeout(async () => {
      if (deleteIntervalRef.current) {
        clearInterval(deleteIntervalRef.current);
        deleteIntervalRef.current = null;
      }
      setDeleteCountdown(null);
      try {
        await deleteChapterMutation.mutateAsync(chapterId);
        navigate(backTo);
      } catch {
        // The mutation hook already shows the API error toast.
      }
    }, 5000);
  }

  function cancelDeleteChapter() {
    if (deleteIntervalRef.current) {
      clearInterval(deleteIntervalRef.current);
      deleteIntervalRef.current = null;
    }
    if (deleteTimeoutRef.current) {
      clearTimeout(deleteTimeoutRef.current);
      deleteTimeoutRef.current = null;
    }
    setDeleteCountdown(null);
  }

  return (
    <header className="fixed inset-x-0 top-0 z-50 border-b bg-background/90 shadow-sm backdrop-blur">
      <div className="mx-auto flex min-h-14 max-w-5xl items-center gap-2 px-4">
        <Button asChild className="shrink-0" size="sm" variant="ghost">
          <Link to={backTo}>
            <ArrowLeft />
            <span className="hidden sm:inline">{t('common.back')}</span>
          </Link>
        </Button>
        <div className="ml-auto flex items-center gap-1">
          <ThemeToggle />
          <LanguageSwitcher />
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
          {canEditChapter && editTo ? (
            <Button asChild size="sm" variant="outline">
              <Link to={editTo}>
                <Pencil />
                <span className="hidden sm:inline">{t('chapters.edit')}</span>
              </Link>
            </Button>
          ) : null}
          {canDeleteChapter ? (
            deleteCountdown === null ? (
              <Button
                aria-label={t('chapters.delete')}
                disabled={deleteChapterMutation.isPending}
                size="sm"
                type="button"
                variant="destructive"
                onClick={scheduleDeleteChapter}
              >
                <Trash2 />
                <span className="hidden sm:inline">{t('chapters.delete')}</span>
              </Button>
            ) : (
              <Button
                aria-label={t('chapters.undoDelete')}
                disabled={deleteChapterMutation.isPending}
                size="sm"
                type="button"
                variant="outline"
                onClick={cancelDeleteChapter}
              >
                <span className="font-bold">{deleteCountdown}s</span>
                <span className="hidden sm:inline">{t('chapters.undoDelete')}</span>
              </Button>
            )
          ) : null}
          {user ? (
            <UserMenu
              user={user}
              isLoggingOut={logoutMutation.isPending}
              onLogout={() => logoutMutation.mutateAsync()}
            />
          ) : (
            <>
              <Button asChild className="sm:hidden" size="icon" variant="outline">
                <Link aria-label={t('auth.login')} to="/login">
                  <LogIn />
                </Link>
              </Button>
              <Button asChild className="hidden sm:inline-flex" size="sm" variant="outline">
                <Link to="/login">{t('auth.login')}</Link>
              </Button>
            </>
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
