import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/config/query-keys';
import { getUserProfile } from '../api/user-api';

export function useUserProfile(userId?: string) {
  return useQuery({
    queryKey: [...queryKeys.users, 'profile', userId],
    queryFn: () => getUserProfile(userId ?? ''),
    enabled: Boolean(userId),
  });
}
