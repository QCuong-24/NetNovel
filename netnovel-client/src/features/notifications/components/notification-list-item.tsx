import { Link } from 'react-router-dom';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { cn } from '@/lib/utils';
import type { NotificationItem } from '../types';

type NotificationListItemProps = {
  notification: NotificationItem;
  compact?: boolean;
  markReadLabel: string;
  deleteLabel?: string;
  onDelete?: () => void;
  onMarkRead?: () => void;
};

export function NotificationListItem({
  compact = false,
  deleteLabel,
  markReadLabel,
  notification,
  onDelete,
  onMarkRead,
}: NotificationListItemProps) {
  if (compact) {
    return (
      <div
        className={cn(
          'grid gap-2 rounded-lg border bg-background p-3 text-left transition hover:border-primary/50 hover:bg-accent',
          !notification.isRead && 'border-primary/40 bg-primary/5',
        )}
      >
        <div className="flex items-start justify-between gap-2">
          <div className="flex min-w-0 flex-wrap items-center gap-2">
            <Badge variant={notification.isRead ? 'outline' : 'secondary'}>{notification.type}</Badge>
            {!notification.isRead ? <span className="size-2 rounded-full bg-primary" /> : null}
            <span className="text-xs font-semibold text-muted-foreground">{formatDateTime(notification.createdAt ?? undefined)}</span>
          </div>
          {!notification.isRead ? (
            <Button className="h-7 shrink-0 px-2 text-xs hover:underline" type="button" variant="ghost" onClick={onMarkRead}>
              {markReadLabel}
            </Button>
          ) : null}
        </div>

        {notification.link ? (
          <Link className="grid gap-1 hover:underline" to={notification.link}>
            <p className="line-clamp-1 text-sm font-extrabold">{notification.title}</p>
            <p className="line-clamp-2 text-sm leading-6 text-muted-foreground">{notification.message}</p>
          </Link>
        ) : (
          <div className="grid gap-1">
            <p className="line-clamp-1 text-sm font-extrabold">{notification.title}</p>
            <p className="line-clamp-2 text-sm leading-6 text-muted-foreground">{notification.message}</p>
          </div>
        )}
      </div>
    );
  }

  const actions = (
    <div className="flex shrink-0 flex-wrap justify-end gap-2">
      {!notification.isRead ? (
        <Button className="h-8 px-2 text-xs hover:underline" type="button" variant="ghost" onClick={onMarkRead}>
          {markReadLabel}
        </Button>
      ) : null}
      {deleteLabel && onDelete ? (
        <Button
          className="h-8 px-2 text-xs text-destructive hover:text-destructive hover:underline"
          type="button"
          variant="ghost"
          onClick={onDelete}
        >
          {deleteLabel}
        </Button>
      ) : null}
    </div>
  );

  return (
    <div
      className={cn(
        'grid gap-1 rounded-lg border bg-background p-3 text-left transition hover:border-primary/50 hover:bg-accent',
        !notification.isRead && 'border-primary/40 bg-primary/5',
      )}
    >
      <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
        <div className="grid gap-2">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant={notification.isRead ? 'outline' : 'secondary'}>{notification.type}</Badge>
            {!notification.isRead ? <span className="size-2 rounded-full bg-primary" /> : null}
            <span className="text-xs font-semibold text-muted-foreground">{formatDateTime(notification.createdAt ?? undefined)}</span>
          </div>
          <p className="text-base font-extrabold">{notification.title}</p>
        </div>
        {actions}
      </div>
      {notification.link ? (
        <Link className="grid gap-1 hover:underline" to={notification.link}>
          <p className="whitespace-pre-line text-sm leading-6 text-muted-foreground">{notification.message}</p>
        </Link>
      ) : (
        <p className="whitespace-pre-line text-sm leading-6 text-muted-foreground">{notification.message}</p>
      )}
    </div>
  );
}
