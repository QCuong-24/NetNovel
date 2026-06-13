import { ChevronDown, LibraryBig, Menu, UserRound } from 'lucide-react';
import { Link, NavLink } from 'react-router-dom';
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
import { useTags } from '@/features/novels/hooks/use-novels';
import { cn } from '@/lib/utils';

const navItems = [
  { to: routes.search, key: 'nav.search' },
  { to: routes.collection, key: 'nav.collection' },
  { to: routes.rankings, key: 'nav.rankings' },
  { to: routes.dashboard, key: 'nav.dashboard' },
  { to: routes.notifications, key: 'nav.notifications' },
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
  const { data: tags = [] } = useTags();

  return (
    <Sheet>
      <SheetTrigger asChild>
        <Button className="md:hidden" size="icon" variant="ghost" type="button" aria-label={t('nav.menu')}>
          <Menu />
        </Button>
      </SheetTrigger>
      <SheetContent>
        <div className="grid gap-6">
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
                {t('nav.tags')}
                <ChevronDown className="size-4 transition-transform group-open:rotate-180" />
              </summary>
              <div className="grid max-h-72 gap-1 overflow-y-auto border-t p-2">
                {tags.map((tag) => (
                  <SheetClose asChild key={tag.tagId}>
                    <NavLink
                      className={({ isActive }) =>
                        cn(
                          'rounded-md px-3 py-2 text-sm font-semibold text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground',
                          isActive && 'bg-accent text-accent-foreground',
                        )
                      }
                      to={routes.novelsTag(tag.name)}
                    >
                      {tag.name}
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
