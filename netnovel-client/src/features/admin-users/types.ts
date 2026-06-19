import type { User } from '@/features/auth/types';
import type { PageResponse } from '@/features/novels/types';

export type AdminUserPayload = {
  username?: string;
  email?: string;
  password?: string;
  profilePictureUrl?: string;
  profilePicturePublicId?: string;
  roles?: string[];
};

export type AdminUserPage = PageResponse<User>;
