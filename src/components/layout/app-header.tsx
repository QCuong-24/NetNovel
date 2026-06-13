import { Bell, ChevronDown, LibraryBig, Search } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { useCurrentUser, useLogoutMutation } from '@/features/auth/hooks/use-auth';
import { LanguageSwitcher } from './language-switcher';
import { MobileNav } from './mobile-nav';
import { ThemeToggle } from './theme-toggle';
import { UserMenu } from './user-menu';
import { routes } from '@/config/routes';
import { useTags } from '@/features/novels/hooks/use-novels';
import { cn } from '@/lib/utils';

const navItems = [
  { to: routes.collection, key: 'nav.collection' },
  { to: routes.rankings, key: 'nav.rankings' },
  { to: routes.dashboard, key: 'nav.dashboard' },
];

const libraryItems = [
  { to: routes.novels, key: 'novelList.nav.all' },
  { to: routes.novelsNewest, key: 'novelList.nav.newest' },
  { to: routes.novelsHot, key: 'novelList.nav.hot' },
  { to: routes.novelsCompleted, key: 'novelList.nav.completed' },
];

export function AppHeader() {
  const { t } = useTranslation();
  const location = useLocation();
  const { data: user } = useCurrentUser();
  const { data: tags = [] } = useTags();
  const logoutMutation = useLogoutMutation();
  const dropdownRef = useRef<HTMLDivElement | null>(null);
  const [openDropdown, setOpenDropdown] = useState<'library' | 'tags' | null>(null);
  const isTagsActive = location.pathname.startsWith('/novels/tags');
  const isLibraryActive = location.pathname.startsWith('/novels') && !isTagsActive;

  useEffect(() => {
    setOpenDropdown(null);
  }, [location.pathname]);

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (!dropdownRef.current?.contains(event.target as Node)) {
        setOpenDropdown(null);
      }
    }

    document.addEventListener('pointerdown', handlePointerDown);

    return () => document.removeEventListener('pointerdown', handlePointerDown);
  }, []);

  return (
    <header className="sticky top-0 z-30 border-b bg-background/90 backdrop-blur">
      <div className="mx-auto flex min-h-16 w-full max-w-7xl items-center gap-4 px-4 md:px-6">
        <NavLink to={routes.home} className="flex items-center gap-2 font-extrabold text-primary">
          <LibraryBig className="size-6" />
          <span>NetNovel</span>
        </NavLink>

        <nav className="hidden items-center gap-1 md:flex" ref={dropdownRef}>
          <NavLink
            className={({ isActive }) =>
              cn(
                'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                isActive && 'bg-accent text-accent-foreground',
              )
            }
            to={routes.home}
          >
            {t('nav.home')}
          </NavLink>
          <div className="relative">
            <button
              className={cn(
                'flex cursor-pointer items-center gap-1 rounded-md px-3 py-2 text-sm font-bold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                isLibraryActive && 'bg-accent text-accent-foreground',
              )}
              type="button"
              onClick={() => setOpenDropdown((current) => (current === 'library' ? null : 'library'))}
            >
              {t('nav.library')}
              <ChevronDown className={cn('size-4 transition-transform', openDropdown === 'library' && 'rotate-180')} />
            </button>
            {openDropdown === 'library' ? (
              <div className="absolute left-0 top-11 z-50 grid w-48 gap-1 rounded-lg border bg-background p-2 shadow-2xl">
                {libraryItems.map((item) => (
                  <NavLink
                    end={item.to === routes.novels}
                    className={({ isActive }) =>
                      cn(
                        'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground hover:bg-accent hover:text-accent-foreground',
                        isActive && 'bg-accent text-accent-foreground',
                      )
                    }
                    key={item.to}
                    to={item.to}
                  >
                    {t(item.key)}
                  </NavLink>
                ))}
              </div>
            ) : null}
          </div>
          <div className="relative">
            <button
              className={cn(
                'flex cursor-pointer items-center gap-1 rounded-md px-3 py-2 text-sm font-bold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                isTagsActive && 'bg-accent text-accent-foreground',
              )}
              type="button"
              onClick={() => setOpenDropdown((current) => (current === 'tags' ? null : 'tags'))}
            >
              {t('nav.tags')}
              <ChevronDown className={cn('size-4 transition-transform', openDropdown === 'tags' && 'rotate-180')} />
            </button>
            {openDropdown === 'tags' ? (
              <div className="absolute left-0 top-11 z-50 grid w-[42rem] grid-cols-3 gap-1 rounded-lg border bg-background p-2 shadow-2xl">
                {tags.map((tag) => (
                  <NavLink
                    className={({ isActive }) =>
                      cn(
                        'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground hover:bg-accent hover:text-accent-foreground',
                        isActive && 'bg-accent text-accent-foreground',
                      )
                    }
                    key={tag.tagId}
                    to={routes.novelsTag(tag.name)}
                  >
                    {tag.name}
                  </NavLink>
                ))}
              </div>
            ) : null}
          </div>
          {navItems.map((item) => (
            <NavLink
              className={({ isActive }) =>
                cn(
                  'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                  isActive && 'bg-accent text-accent-foreground',
                )
              }
              key={item.to}
              to={item.to}
            >
              {t(item.key)}
            </NavLink>
          ))}
        </nav>

        <label className="ml-auto hidden h-10 min-w-64 items-center gap-2 rounded-md border bg-card px-3 text-muted-foreground lg:flex">
          <Search className="size-4" />
          <input
            className="w-full bg-transparent text-sm text-foreground outline-none"
            placeholder={t('common.searchPlaceholder')}
          />
        </label>

        <div className="ml-auto flex items-center gap-1 lg:ml-0">
          <ThemeToggle />
          <LanguageSwitcher />
          <Button aria-label={t('nav.notifications')} size="icon" variant="ghost" asChild>
            <Link to={routes.notifications}>
              <Bell />
            </Link>
          </Button>
          {user ? (
            <UserMenu
              user={user}
              isLoggingOut={logoutMutation.isPending}
              onLogout={() => logoutMutation.mutateAsync()}
            />
          ) : (
            <Button className="hidden md:inline-flex" variant="outline" asChild>
              <Link to={routes.login}>{t('auth.login')}</Link>
            </Button>
          )}
          <MobileNav user={user} />
        </div>
      </div>
    </header>
  );
}
