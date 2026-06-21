import { Outlet } from 'react-router-dom';
import { ScrollToTop } from '@/components/layout/scroll-to-top';
import { BackToTopButton } from '@/components/layout/back-to-top-button';

export function AuthLayout() {
  return (
    <main className="grid min-h-screen place-items-center bg-background px-4 py-8 text-foreground">
      <ScrollToTop />
      <Outlet />
      <BackToTopButton />
    </main>
  );
}
