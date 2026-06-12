import type { User } from '@/features/auth/types';

export function canManageNovels(user?: User) {
  return Boolean(user?.roles?.some((role) => role === 'MANAGER' || role === 'ADMIN'));
}
