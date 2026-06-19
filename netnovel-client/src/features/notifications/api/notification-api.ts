import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { NotificationCreatePayload, NotificationItem, NotificationListParams, NotificationPage } from '../types';

function withNotificationParams(params: NotificationListParams = {}) {
  const searchParams = new URLSearchParams();
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 10));

  if (params.filter === 'read') {
    searchParams.set('isRead', 'true');
  }
  if (params.filter === 'unread') {
    searchParams.set('isRead', 'false');
  }
  if (params.type) {
    searchParams.set('type', params.type);
  }

  return searchParams.toString();
}

export async function getNotifications(params: NotificationListParams = {}) {
  const endpoint = params.filter === 'unread' && !params.type ? endpoints.notifications.unread : endpoints.notifications.list;
  const response = await httpClient.get<NotificationPage>(`${endpoint}?${withNotificationParams(params)}`);

  return response.data;
}

export async function getUnreadNotificationCount() {
  const response = await httpClient.get<{ count: number }>(endpoints.notifications.unreadCount);

  return response.data.count;
}

export async function markNotificationRead(notificationId: string) {
  const response = await httpClient.patch<NotificationItem>(endpoints.notifications.markRead(notificationId));

  return response.data;
}

export async function markAllNotificationsRead() {
  await httpClient.patch(endpoints.notifications.markAllRead);
}

export async function deleteNotification(notificationId: string) {
  await httpClient.delete(endpoints.notifications.delete(notificationId));
}

export async function deleteAllNotifications() {
  await httpClient.delete(endpoints.notifications.deleteAll);
}

export async function sendNotificationToUser(userId: string, payload: NotificationCreatePayload) {
  const response = await httpClient.post<NotificationItem>(endpoints.notifications.sendToUser(userId), payload);

  return response.data;
}
