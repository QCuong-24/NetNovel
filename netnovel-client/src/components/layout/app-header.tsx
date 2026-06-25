import { ChevronDown, Home, LibraryBig, Search } from 'lucide-react';
import { FormEvent, KeyboardEvent, useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { useCurrentUser, useLogoutMutation } from '@/features/auth/hooks/use-auth';
import { NotificationDropdown } from '@/features/notifications/components/notification-dropdown';
import { useSearchSuggestions } from '@/features/search/hooks/use-search';
import type { SearchSuggestion } from '@/features/search/types';
import { LanguageSwitcher } from './language-switcher';
import { MobileNav } from './mobile-nav';
import { ThemeToggle } from './theme-toggle';
import { UserMenu } from './user-menu';
import { routes } from '@/config/routes';
import { useGenres } from '@/features/novels/hooks/use-novels';
import { cn } from '@/lib/utils';

const navItems = [
  { to: routes.search, key: 'nav.search' },
  { to: routes.rankings, key: 'nav.rankings' },
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
  const navigate = useNavigate();
  const { data: user } = useCurrentUser();
  const { data: genres = [] } = useGenres();
  const logoutMutation = useLogoutMutation();
  const dropdownRef = useRef<HTMLDivElement | null>(null);
  const searchRef = useRef<HTMLFormElement | null>(null);
  const [openDropdown, setOpenDropdown] = useState<'library' | 'genres' | null>(null);
  const [searchText, setSearchText] = useState('');
  const [debouncedSearchText, setDebouncedSearchText] = useState('');
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [selectedSearchIndex, setSelectedSearchIndex] = useState(-1);
  const isGenresActive = location.pathname.startsWith('/novels/genres');
  const isLibraryActive = location.pathname.startsWith('/novels') && !isGenresActive;
  const searchSuggestionsQuery = useSearchSuggestions(debouncedSearchText, isSearchFocused);
  const searchSuggestions = searchSuggestionsQuery.data ?? [];
  const normalizedSearchText = searchText.trim();
  const shouldShowSearchDropdown = isSearchFocused && Boolean(debouncedSearchText.trim());
  const searchOptionCount = searchSuggestions.length || (normalizedSearchText ? 1 : 0);
  const canUseDashboard = Boolean(user?.roles?.some((role) => role === 'MANAGER' || role === 'ADMIN'));

  useEffect(() => {
    setOpenDropdown(null);
    setIsSearchFocused(false);
  }, [location.pathname]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setDebouncedSearchText(searchText.trim());
    }, 1000);

    return () => window.clearTimeout(timeoutId);
  }, [searchText]);

  useEffect(() => {
    setSelectedSearchIndex(-1);
  }, [debouncedSearchText, searchSuggestions.length]);

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (!dropdownRef.current?.contains(event.target as Node)) {
        setOpenDropdown(null);
      }

      if (!searchRef.current?.contains(event.target as Node)) {
        setIsSearchFocused(false);
      }
    }

    document.addEventListener('pointerdown', handlePointerDown);

    return () => document.removeEventListener('pointerdown', handlePointerDown);
  }, []);

  function goToSearch(query: string) {
    const normalizedQuery = query.trim();

    if (!normalizedQuery) {
      return;
    }

    setIsSearchFocused(false);
    navigate(`${routes.search}?q=${encodeURIComponent(normalizedQuery)}`);
  }

  function getSuggestionTarget(suggestion: SearchSuggestion) {
    if (suggestion.type === 'NOVEL' && suggestion.id) {
      return `/novels/${suggestion.id}`;
    }

    if (suggestion.type === 'GENRE') {
      return routes.novelsGenre(suggestion.label);
    }

    return `${routes.search}?q=${encodeURIComponent(suggestion.label)}`;
  }

  function chooseSuggestion(suggestion: SearchSuggestion) {
    setSearchText(suggestion.label);
    setIsSearchFocused(false);
    navigate(getSuggestionTarget(suggestion));
  }

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    goToSearch(searchText);
  }

  function handleSearchKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Escape') {
      setIsSearchFocused(false);
      setSelectedSearchIndex(-1);
      return;
    }

    if (!shouldShowSearchDropdown || searchOptionCount <= 0) {
      return;
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setSelectedSearchIndex((current) => (current + 1) % searchOptionCount);
      return;
    }

    if (event.key === 'ArrowUp') {
      event.preventDefault();
      setSelectedSearchIndex((current) => (current <= 0 ? searchOptionCount - 1 : current - 1));
      return;
    }

    if (event.key === 'Enter' && selectedSearchIndex >= 0) {
      event.preventDefault();
      const selectedSuggestion = searchSuggestions[selectedSearchIndex];

      if (selectedSuggestion) {
        chooseSuggestion(selectedSuggestion);
      } else {
        goToSearch(normalizedSearchText);
      }
    }
  }

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
            aria-label={t('nav.home')}
            title={t('nav.home')}
            to={routes.home}
          >
            <Home className="size-4" />
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
                isGenresActive && 'bg-accent text-accent-foreground',
              )}
              type="button"
              onClick={() => setOpenDropdown((current) => (current === 'genres' ? null : 'genres'))}
            >
              {t('nav.genres')}
              <ChevronDown className={cn('size-4 transition-transform', openDropdown === 'genres' && 'rotate-180')} />
            </button>
            {openDropdown === 'genres' ? (
              <div className="absolute left-0 top-11 z-50 grid w-[42rem] grid-cols-3 gap-1 rounded-lg border bg-background p-2 shadow-2xl">
                {genres.map((genre) => (
                  <NavLink
                    className={({ isActive }) =>
                      cn(
                        'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground hover:bg-accent hover:text-accent-foreground',
                        isActive && 'bg-accent text-accent-foreground',
                      )
                    }
                    key={genre.genreId}
                    to={routes.novelsGenre(genre.name)}
                  >
                    {genre.name}
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
          {user ? (
            <NavLink
              className={({ isActive }) =>
                cn(
                  'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                  isActive && 'bg-accent text-accent-foreground',
                )
              }
              to={routes.collection}
            >
              {t('nav.collection')}
            </NavLink>
          ) : null}
          {canUseDashboard ? (
            <NavLink
              className={({ isActive }) =>
                cn(
                  'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                  isActive && 'bg-accent text-accent-foreground',
                )
              }
              to={routes.dashboard}
            >
              {t('nav.dashboard')}
            </NavLink>
          ) : null}
        </nav>

        <form
          className="relative ml-auto hidden h-10 min-w-64 items-center gap-2 rounded-md border bg-card px-3 text-muted-foreground lg:flex"
          ref={searchRef}
          onSubmit={handleSearchSubmit}
        >
          <Search className="size-4" />
          <input
            className="w-full bg-transparent text-sm text-foreground outline-none"
            placeholder={t('common.searchPlaceholder')}
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            onFocus={() => setIsSearchFocused(true)}
            onKeyDown={handleSearchKeyDown}
          />
          {shouldShowSearchDropdown ? (
            <div className="absolute left-0 right-0 top-12 z-50 overflow-hidden rounded-lg border bg-background shadow-2xl">
              {searchSuggestionsQuery.isLoading ? (
                <div className="px-3 py-3 text-sm font-semibold text-muted-foreground">{t('searchPage.loading')}</div>
              ) : searchSuggestions.length ? (
                <div className="grid py-1">
                  {searchSuggestions.map((suggestion) => {
                    const suggestionTarget = getSuggestionTarget(suggestion);
                    const suggestionIndex = searchSuggestions.indexOf(suggestion);

                    return (
                      <Link
                        className={cn(
                          'grid gap-0.5 px-3 py-2 text-sm text-foreground hover:bg-accent hover:text-accent-foreground',
                          selectedSearchIndex === suggestionIndex && 'bg-accent text-accent-foreground',
                        )}
                        key={`${suggestion.type}-${suggestion.id ?? suggestion.label}`}
                        to={suggestionTarget}
                        onClick={() => {
                          setSearchText(suggestion.label);
                          setIsSearchFocused(false);
                        }}
                        onMouseEnter={() => setSelectedSearchIndex(suggestionIndex)}
                      >
                        <span className="font-semibold">{suggestion.label}</span>
                        <span className="text-xs uppercase text-muted-foreground">{suggestion.type}</span>
                      </Link>
                    );
                  })}
                </div>
              ) : (
                <button
                  className={cn(
                    'flex w-full items-center gap-2 px-3 py-3 text-left text-sm font-semibold text-muted-foreground hover:bg-accent hover:text-accent-foreground',
                    selectedSearchIndex === 0 && 'bg-accent text-accent-foreground',
                  )}
                  type="button"
                  onClick={() => goToSearch(normalizedSearchText)}
                  onMouseEnter={() => setSelectedSearchIndex(0)}
                >
                  <Search className="size-4" />
                  {normalizedSearchText}
                </button>
              )}
            </div>
          ) : null}
        </form>

        <div className="ml-auto flex items-center gap-1 lg:ml-0">
          <ThemeToggle />
          <LanguageSwitcher />
          {user ? <NotificationDropdown /> : null}
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
