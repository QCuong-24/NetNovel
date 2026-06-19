import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { routes } from '@/config/routes';
import {
  useMarkNotificationReadMutation,
  useNotifications,
  useUnreadNotificationCount,
} from '../hooks/use-notifications';
import type { NotificationFilter } from '../types';
import { NotificationListItem } from './notification-list-item';

export function NotificationDropdown() {
  const { t } = useTranslation();
  const location = useLocation();
  const dropdownRef = useRef<HTMLDivElement | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [filter, setFilter] = useState<Extract<NotificationFilter, 'all' | 'unread'>>('all');
  const { data: unreadCount = 0 } = useUnreadNotificationCount();
  const notificationsQuery = useNotifications({ filter, page: 0, size: 5 });
  const markReadMutation = useMarkNotificationReadMutation();
  const notifications = notificationsQuery.data?.content ?? [];

  useEffect(() => {
    setIsOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (!dropdownRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    document.addEventListener('pointerdown', handlePointerDown);

    return () => document.removeEventListener('pointerdown', handlePointerDown);
  }, []);

  return (
    <div className="relative" ref={dropdownRef}>
      <Button aria-label={t('nav.notifications')} className="relative" size="icon" type="button" variant="ghost" onClick={() => setIsOpen((current) => !current)}>
        <Bell />
        {unreadCount > 0 ? (
          <span className="absolute right-1 top-1 grid min-w-4 place-items-center rounded-full bg-destructive px-1 text-[10px] font-extrabold text-destructive-foreground">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        ) : null}
      </Button>

      {isOpen ? (
        <div className="absolute right-0 top-12 z-50 grid w-[min(92vw,24rem)] gap-3 rounded-lg border border-border/80 bg-background p-3 text-foreground shadow-2xl ring-1 ring-black/10 dark:ring-white/15">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-extrabold">{t('notifications.title')}</p>
              <p className="text-xs font-semibold text-muted-foreground">{t('notifications.unreadCount', { count: unreadCount })}</p>
            </div>
            <select
              className="h-9 rounded-md border bg-background px-2 text-sm font-semibold outline-none focus-visible:ring-2 focus-visible:ring-ring"
              value={filter}
              onChange={(event) => setFilter(event.target.value as Extract<NotificationFilter, 'all' | 'unread'>)}
            >
              <option value="all">{t('notifications.filters.all')}</option>
              <option value="unread">{t('notifications.filters.unread')}</option>
            </select>
          </div>

          <div className="grid max-h-96 gap-3 overflow-y-auto pr-1">
            {notificationsQuery.isLoading ? (
              <p className="py-6 text-center text-sm font-semibold text-muted-foreground">{t('notifications.loading')}</p>
            ) : notifications.length ? (
              notifications.map((notification) => (
                <NotificationListItem
                  compact
                  key={notification.notificationId}
                  markReadLabel={t('notifications.markRead')}
                  notification={notification}
                  onMarkRead={() => markReadMutation.mutate(String(notification.notificationId))}
                />
              ))
            ) : (
              <p className="py-6 text-center text-sm font-semibold text-muted-foreground">{t('notifications.empty')}</p>
            )}
          </div>

          <Button asChild>
            <Link to={routes.notifications}>{t('notifications.openPage')}</Link>
          </Button>
        </div>
      ) : null}
    </div>
  );
}
