import { Outlet } from 'react-router-dom';
import { ReaderFooter } from '@/components/layout/reader-footer';

export function ReaderLayout() {
  return (
    <main className="grid min-h-screen grid-rows-[1fr_auto]">
      <Outlet />
      <ReaderFooter />
    </main>
  );
}
