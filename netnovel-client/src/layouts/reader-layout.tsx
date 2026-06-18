import { Outlet } from 'react-router-dom';
import { ReaderFooter } from '@/components/layout/reader-footer';
import { ScrollToTop } from '@/components/layout/scroll-to-top';

export function ReaderLayout() {
  return (
    <main className="grid min-h-screen grid-rows-[1fr_auto]">
      <ScrollToTop />
      <Outlet />
      <ReaderFooter />
    </main>
  );
}
