import { Outlet } from 'react-router-dom';
import { ReaderFooter } from '@/components/layout/reader-footer';
import { BackToTopButton } from '@/components/layout/back-to-top-button';
import { ScrollToTop } from '@/components/layout/scroll-to-top';

export function ReaderLayout() {
  return (
    <main className="grid min-h-screen min-w-0 grid-rows-[1fr_auto] overflow-x-hidden">
      <ScrollToTop />
      <Outlet />
      <ReaderFooter />
      <BackToTopButton />
    </main>
  );
}
