import { LibraryBig } from 'lucide-react';
import { Link } from 'react-router-dom';
import { routes } from '@/config/routes';

export function ReaderFooter() {
  return (
    <footer className="border-t bg-background/90 text-foreground">
      <div className="mx-auto flex min-h-16 max-w-5xl flex-col justify-center gap-2 px-4 py-4 text-sm text-muted-foreground sm:flex-row sm:items-center sm:justify-between">
        <Link className="flex items-center gap-2 font-extrabold text-primary" to={routes.home}>
          <LibraryBig className="size-5" />
          <span>NetNovel</span>
        </Link>
        <span>Comfort reading mode</span>
      </div>
    </footer>
  );
}
