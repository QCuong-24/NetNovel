import { Bell, Bookmark, DatabaseZap, LayoutDashboard, LogOut, Upload, UserRound } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { routes } from '@/config/routes';
import type { User } from '@/features/auth/types';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';

type UserMenuProps = {
  user: User;
  isLoggingOut?: boolean;
  onLogout: () => Promise<unknown> | void;
};

function getInitial(username: string) {
  return username.trim().charAt(0).toUpperCase() || 'U';
}

export function UserMenu({ user, isLoggingOut = false, onLogout }: UserMenuProps) {
  const { t } = useTranslation();
  const location = useLocation();
  const menuRef = useRef<HTMLDivElement | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const canCreateNovel = canManageNovels(user);

  useEffect(() => {
    setIsOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (!menuRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    document.addEventListener('pointerdown', handlePointerDown);

    return () => document.removeEventListener('pointerdown', handlePointerDown);
  }, []);

  async function handleLogout() {
    try {
      await onLogout();
    } finally {
      window.location.reload();
    }
  }

  return (
    <div className="relative" ref={menuRef}>
      <button
        className="flex size-10 cursor-pointer list-none items-center justify-center overflow-hidden rounded-full border bg-card text-sm font-extrabold text-primary shadow-sm transition-[background-color,transform] duration-150 ease-out hover:scale-105 hover:bg-accent active:scale-95 [&::-webkit-details-marker]:hidden"
        aria-label={t('nav.profile')}
        type="button"
        onClick={() => setIsOpen((current) => !current)}
      >
        {user.profilePictureUrl ? (
          <img alt={user.username} className="h-full w-full object-cover" src={user.profilePictureUrl} />
        ) : (
          getInitial(user.username)
        )}
      </button>

      {isOpen ? (
        <div className="dropdown-motion absolute right-0 top-12 z-50 grid w-60 origin-top-right gap-1 rounded-lg border border-border/80 bg-background p-2 text-foreground shadow-2xl ring-1 ring-black/10 dark:ring-white/15">
          <div className="border-b px-3 py-2">
            <p className="truncate text-sm font-bold">{user.username}</p>
            <p className="truncate text-xs text-muted-foreground">{user.email}</p>
          </div>

          <Button asChild className="justify-start" variant="ghost">
            <Link to={routes.profile}>
              <UserRound />
              {t('nav.profile')}
            </Link>
          </Button>
          <Button asChild className="justify-start" variant="ghost">
            <Link to={routes.notifications}>
              <Bell />
              {t('nav.notifications')}
            </Link>
          </Button>
          <Button asChild className="justify-start" variant="ghost">
            <Link to={routes.collection}>
              <Bookmark />
              {t('nav.collection')}
            </Link>
          </Button>
          {canCreateNovel ? (
            <>
              <Button asChild className="justify-start" variant="ghost">
                <Link to={routes.dashboard}>
                  <LayoutDashboard />
                  {t('nav.dashboard')}
                </Link>
              </Button>
              <Button asChild className="justify-start" variant="ghost">
                <Link to={routes.crawlTasks}>
                  <DatabaseZap />
                  {t('crawlTasks.menu')}
                </Link>
              </Button>
              <Button asChild className="justify-start" variant="ghost">
                <Link to={routes.novelNew}>
                  <Upload />
                  {t('common.upload')}
                </Link>
              </Button>
            </>
          ) : null}
          <Button
            className="justify-start text-destructive hover:text-destructive"
            disabled={isLoggingOut}
            type="button"
            variant="ghost"
            onClick={handleLogout}
          >
            <LogOut />
            {t('auth.logout')}
          </Button>
        </div>
      ) : null}
    </div>
  );
}
