import { BookOpen } from 'lucide-react';
import { cn } from '@/lib/utils';

type NovelCoverProps = {
  src?: string | null;
  title: string;
  className?: string;
};

export function NovelCover({ src, title, className }: NovelCoverProps) {
  if (src) {
    return (
      <img
        alt={title}
        className={cn('aspect-[3/4] w-full rounded-lg object-cover shadow-sm', className)}
        src={src}
      />
    );
  }

  return (
    <div
      className={cn(
        'flex aspect-[3/4] w-full items-center justify-center rounded-lg bg-cover-gradient text-primary-foreground shadow-sm',
        className,
      )}
      aria-label={title}
      role="img"
    >
      <BookOpen className="size-14" />
    </div>
  );
}
