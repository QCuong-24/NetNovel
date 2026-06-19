import type { PageResponse } from '@/features/novels/types';

export type NotificationFilter = 'all' | 'unread' | 'read';

export type NotificationItem = {
  notificationId: number;
  userId: number;
  type: string;
  title: string;
  message: string;
  link?: string | null;
  isRead?: boolean | null;
  createdAt?: string | null;
};

export type NotificationPage = PageResponse<NotificationItem>;

export type NotificationListParams = {
  filter?: NotificationFilter;
  type?: string;
  page?: number;
  size?: number;
};

export type NotificationCreatePayload = {
  type: string;
  title: string;
  message: string;
  link?: string;
};
