import { useEffect } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { env } from '@/config/env';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { getAccessToken, hasAuthTokens } from '@/features/auth/lib/auth-storage';
import {
  deleteAllNotifications,
  deleteNotification,
  getNotifications,
  getUnreadNotificationCount,
  markAllNotificationsRead,
  markNotificationRead,
  sendNotificationToUser,
} from '../api/notification-api';
import type { NotificationCreatePayload, NotificationItem, NotificationListParams } from '../types';

export function useNotifications(params: NotificationListParams) {
  return useQuery({
    queryKey: [...queryKeys.notifications, 'list', params],
    queryFn: () => getNotifications(params),
  });
}

export function useUnreadNotificationCount(enabled = true) {
  return useQuery({
    queryKey: [...queryKeys.notifications, 'unreadCount'],
    queryFn: getUnreadNotificationCount,
    enabled,
  });
}

export function useMarkNotificationReadMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: markNotificationRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not mark notification as read'));
    },
  });
}

export function useMarkAllNotificationsReadMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
      toast.success('Notifications marked as read');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not mark notifications as read'));
    },
  });
}

export function useDeleteNotificationMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
      toast.success('Notification deleted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not delete notification'));
    },
  });
}

export function useDeleteAllNotificationsMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteAllNotifications,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
      toast.success('Notifications deleted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not delete notifications'));
    },
  });
}

export function useSendNotificationToUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, payload }: { userId: string; payload: NotificationCreatePayload }) =>
      sendNotificationToUser(userId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });
      toast.success('Notification sent');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not send notification'));
    },
  });
}

export function useNotificationStream(enabled = true) {
  const queryClient = useQueryClient();

  useEffect(() => {
    const accessToken = getAccessToken();

    if (!enabled || !hasAuthTokens() || !accessToken) {
      return;
    }

    const apiBaseUrl = env.apiBaseUrl.replace(/\/$/, '');
    const streamUrl = new URL(`${apiBaseUrl}/notifications/stream`, window.location.origin);
    streamUrl.searchParams.set('access_token', accessToken);

    const eventSource = new EventSource(streamUrl.toString());

    eventSource.addEventListener('notification', (event) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications });

      try {
        const notification = JSON.parse(event.data) as NotificationItem;
        toast(notification.title, {
          description: notification.message,
        });
      } catch {
        toast('New notification');
      }
    });

    eventSource.onerror = () => {
      eventSource.close();
    };

    return () => eventSource.close();
  }, [enabled, queryClient]);
}
