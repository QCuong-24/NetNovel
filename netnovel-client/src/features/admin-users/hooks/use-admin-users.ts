import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { createAdminUser, deleteAdminUser, getAdminUser, getAdminUsers, updateAdminUser } from '../api/admin-user-api';
import type { AdminUserPayload } from '../types';

type AdminUserListParams = {
  page?: number;
  size?: number;
};

export function useAdminUsers(params: AdminUserListParams) {
  return useQuery({
    queryKey: [...queryKeys.adminUsers, 'list', params],
    queryFn: () => getAdminUsers(params),
  });
}

export function useAdminUser(userId?: string) {
  return useQuery({
    queryKey: [...queryKeys.adminUsers, 'detail', userId],
    queryFn: () => getAdminUser(userId ?? ''),
    enabled: Boolean(userId),
  });
}

export function useCreateAdminUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createAdminUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminUsers });
      toast.success('User created');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not create user'));
    },
  });
}

export function useUpdateAdminUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, payload }: { userId: string; payload: AdminUserPayload }) => updateAdminUser(userId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminUsers });
      toast.success('User updated');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update user'));
    },
  });
}

export function useDeleteAdminUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteAdminUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.adminUsers });
      toast.success('User deleted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not delete user'));
    },
  });
}
