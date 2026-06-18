import { ChevronDown, LibraryBig, Menu, Search, UserRound } from 'lucide-react';
import { FormEvent, KeyboardEvent, useEffect, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetDescription,
  SheetTitle,
  SheetTrigger,
} from '@/components/ui/sheet';
import { routes } from '@/config/routes';
import type { User } from '@/features/auth/types';
import { useGenres } from '@/features/novels/hooks/use-novels';
import { useSearchSuggestions } from '@/features/search/hooks/use-search';
import type { SearchSuggestion } from '@/features/search/types';
import { cn } from '@/lib/utils';

const navItems = [
  { to: routes.search, key: 'nav.search' },
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

type MobileNavProps = {
  user?: User;
};

export function MobileNav({ user }: MobileNavProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { data: genres = [] } = useGenres();
  const [open, setOpen] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [debouncedSearchText, setDebouncedSearchText] = useState('');
  const [selectedSearchIndex, setSelectedSearchIndex] = useState(-1);
  const searchSuggestionsQuery = useSearchSuggestions(debouncedSearchText, open);
  const searchSuggestions = searchSuggestionsQuery.data ?? [];
  const normalizedSearchText = searchText.trim();
  const shouldShowSearchDropdown = open && Boolean(debouncedSearchText.trim());
  const searchOptionCount = searchSuggestions.length || (normalizedSearchText ? 1 : 0);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setDebouncedSearchText(searchText.trim());
    }, 1000);

    return () => window.clearTimeout(timeoutId);
  }, [searchText]);

  useEffect(() => {
    setSelectedSearchIndex(-1);
  }, [debouncedSearchText, searchSuggestions.length]);

  function goToSearch(query: string) {
    const normalizedQuery = query.trim();

    if (!normalizedQuery) {
      return;
    }

    setOpen(false);
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
    setOpen(false);
    navigate(getSuggestionTarget(suggestion));
  }

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    goToSearch(searchText);
  }

  function handleSearchKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Escape') {
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
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger asChild>
        <Button className="md:hidden" size="icon" variant="ghost" type="button" aria-label={t('nav.menu')}>
          <Menu />
        </Button>
      </SheetTrigger>
      <SheetContent className="overflow-y-auto">
        <div className="grid min-h-full content-start gap-6 pb-4">
          <div className="grid gap-1 pr-10">
            <SheetTitle className="flex items-center gap-2 text-left text-lg font-extrabold text-primary">
              <LibraryBig className="size-6" />
              NetNovel
            </SheetTitle>
            <SheetDescription className="text-left text-sm text-muted-foreground">
              {t('footer.description')}
            </SheetDescription>
          </div>

          <nav className="grid gap-1">
            <form className="relative mb-2 flex h-11 items-center gap-2 rounded-md border bg-card px-3 text-muted-foreground" onSubmit={handleSearchSubmit}>
              <Search className="size-4 shrink-0" />
              <input
                className="w-full bg-transparent text-sm text-foreground outline-none"
                placeholder={t('common.searchPlaceholder')}
                value={searchText}
                onChange={(event) => setSearchText(event.target.value)}
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
                          <SheetClose asChild key={`${suggestion.type}-${suggestion.id ?? suggestion.label}`}>
                            <Link
                              className={cn(
                                'grid gap-0.5 px-3 py-2 text-sm text-foreground hover:bg-accent hover:text-accent-foreground',
                                selectedSearchIndex === suggestionIndex && 'bg-accent text-accent-foreground',
                              )}
                              to={suggestionTarget}
                              onClick={() => setSearchText(suggestion.label)}
                              onMouseEnter={() => setSelectedSearchIndex(suggestionIndex)}
                            >
                              <span className="font-semibold">{suggestion.label}</span>
                              <span className="text-xs uppercase text-muted-foreground">{suggestion.type}</span>
                            </Link>
                          </SheetClose>
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
            <SheetClose asChild>
              <NavLink
                className={({ isActive }) =>
                  cn(
                    'rounded-md px-3 py-3 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                    isActive && 'bg-accent text-accent-foreground',
                  )
                }
                to={routes.home}
              >
                {t('nav.home')}
              </NavLink>
            </SheetClose>
            <details className="group rounded-md border">
              <summary className="flex cursor-pointer list-none items-center justify-between px-3 py-3 text-sm font-semibold text-muted-foreground [&::-webkit-details-marker]:hidden">
                {t('nav.library')}
                <ChevronDown className="size-4 transition-transform group-open:rotate-180" />
              </summary>
              <div className="grid gap-1 border-t p-2">
                {libraryItems.map((item) => (
                  <SheetClose asChild key={item.to}>
                    <NavLink
                      className={({ isActive }) =>
                        cn(
                          'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                          isActive && 'bg-accent text-accent-foreground',
                        )
                      }
                      to={item.to}
                    >
                      {t(item.key)}
                    </NavLink>
                  </SheetClose>
                ))}
              </div>
            </details>
            <details className="group rounded-md border">
              <summary className="flex cursor-pointer list-none items-center justify-between px-3 py-3 text-sm font-semibold text-muted-foreground [&::-webkit-details-marker]:hidden">
                {t('nav.genres')}
                <ChevronDown className="size-4 transition-transform group-open:rotate-180" />
              </summary>
              <div className="grid max-h-72 gap-1 overflow-y-auto border-t p-2">
                {genres.map((genre) => (
                  <SheetClose asChild key={genre.genreId}>
                    <NavLink
                      className={({ isActive }) =>
                        cn(
                          'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                          isActive && 'bg-accent text-accent-foreground',
                        )
                      }
                      to={routes.novelsGenre(genre.name)}
                    >
                      {genre.name}
                    </NavLink>
                  </SheetClose>
                ))}
              </div>
            </details>
            {navItems.map((item) => (
              <SheetClose asChild key={item.to}>
                <NavLink
                  className={({ isActive }) =>
                    cn(
                      'rounded-md px-3 py-3 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                      isActive && 'bg-accent text-accent-foreground',
                    )
                  }
                  to={item.to}
                >
                  {t(item.key)}
                </NavLink>
              </SheetClose>
            ))}
          </nav>

          {!user ? (
            <div className="grid gap-3 border-t pt-4">
              <Button className="justify-start" variant="outline" asChild>
                <Link to={routes.login}>
                  <UserRound />
                  {t('auth.login')}
                </Link>
              </Button>
            </div>
          ) : null}
        </div>
      </SheetContent>
    </Sheet>
  );
}
