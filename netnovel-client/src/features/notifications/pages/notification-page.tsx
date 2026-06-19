import { useState } from 'react';
import { Bell, Trash2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import {
  useDeleteAllNotificationsMutation,
  useDeleteNotificationMutation,
  useMarkAllNotificationsReadMutation,
  useMarkNotificationReadMutation,
  useNotifications,
} from '../hooks/use-notifications';
import type { NotificationFilter } from '../types';
import { NotificationListItem } from '../components/notification-list-item';

export function NotificationPage() {
  const { t } = useTranslation();
  const [filter, setFilter] = useState<NotificationFilter>('all');
  const [type, setType] = useState('');
  const [page, setPage] = useState(0);
  const notificationsQuery = useNotifications({ filter, type: type.trim() || undefined, page, size: 10 });
  const markReadMutation = useMarkNotificationReadMutation();
  const markAllReadMutation = useMarkAllNotificationsReadMutation();
  const deleteMutation = useDeleteNotificationMutation();
  const deleteAllMutation = useDeleteAllNotificationsMutation();
  const notificationsPage = notificationsQuery.data;
  const notifications = notificationsPage?.content ?? [];

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <header className="grid gap-2">
        <p className="text-sm font-semibold uppercase text-primary">{t('nav.notifications')}</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">{t('notifications.title')}</h1>
        <p className="max-w-3xl text-sm leading-6 text-muted-foreground">{t('notifications.description')}</p>
      </header>

      <Card>
        <CardHeader className="gap-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <CardTitle className="flex items-center gap-2">
              <Bell className="size-5 text-primary" />
              {t('notifications.inbox')}
            </CardTitle>
            <div className="flex flex-wrap gap-2">
              <Button disabled={markAllReadMutation.isPending} type="button" variant="outline" onClick={() => markAllReadMutation.mutate()}>
                {t('notifications.markAllRead')}
              </Button>
              <Button disabled={deleteAllMutation.isPending} type="button" variant="destructive" onClick={() => deleteAllMutation.mutate()}>
                <Trash2 />
                {t('notifications.deleteAll')}
              </Button>
            </div>
          </div>
          <div className="grid gap-3 sm:grid-cols-[180px_minmax(0,1fr)]">
            <select
              className="h-10 rounded-md border bg-background px-3 text-sm font-semibold outline-none focus-visible:ring-2 focus-visible:ring-ring"
              value={filter}
              onChange={(event) => {
                setPage(0);
                setFilter(event.target.value as NotificationFilter);
              }}
            >
              <option value="all">{t('notifications.filters.all')}</option>
              <option value="unread">{t('notifications.filters.unread')}</option>
              <option value="read">{t('notifications.filters.read')}</option>
            </select>
            <Input
              placeholder={t('notifications.typePlaceholder')}
              value={type}
              onChange={(event) => {
                setPage(0);
                setType(event.target.value);
              }}
            />
          </div>
        </CardHeader>
        <CardContent className="grid gap-4">
          {notificationsQuery.isLoading ? (
            <div className="grid min-h-48 place-items-center text-sm font-semibold text-muted-foreground">{t('notifications.loading')}</div>
          ) : notifications.length ? (
            notifications.map((notification) => (
              <NotificationListItem
                deleteLabel={t('notifications.delete')}
                key={notification.notificationId}
                markReadLabel={t('notifications.markRead')}
                notification={notification}
                onDelete={() => deleteMutation.mutate(String(notification.notificationId))}
                onMarkRead={() => markReadMutation.mutate(String(notification.notificationId))}
              />
            ))
          ) : (
            <div className="grid min-h-48 place-items-center rounded-lg border border-dashed text-sm font-semibold text-muted-foreground">
              {t('notifications.empty')}
            </div>
          )}

          {notificationsPage && notificationsPage.totalPages > 1 ? (
            <div className="flex flex-col gap-2 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
              <span className="text-sm font-bold text-muted-foreground">
                {t('notifications.pageInfo', { page: notificationsPage.number + 1, total: notificationsPage.totalPages })}
              </span>
              <div className="flex gap-2">
                <Button disabled={notificationsPage.first || notificationsQuery.isLoading} type="button" variant="outline" onClick={() => setPage((current) => Math.max(0, current - 1))}>
                  {t('rankingPage.previous')}
                </Button>
                <Button disabled={notificationsPage.last || notificationsQuery.isLoading} type="button" variant="outline" onClick={() => setPage((current) => current + 1)}>
                  {t('rankingPage.next')}
                </Button>
              </div>
            </div>
          ) : null}
        </CardContent>
      </Card>
    </main>
  );
}
