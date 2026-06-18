import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { clearAuthTokens, getRefreshToken, hasAuthTokens, saveAuthTokens } from '../lib/auth-storage';
import { getCurrentUser, login, loginWithGoogle, logout, register } from '../api/auth-api';

export function useCurrentUser() {
  return useQuery({
    queryKey: queryKeys.auth,
    queryFn: getCurrentUser,
    enabled: hasAuthTokens(),
    retry: false,
  });
}

export function useLoginMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: login,
    onSuccess: (response) => {
      saveAuthTokens(response);
      queryClient.setQueryData(queryKeys.auth, response.user);
      toast.success(response.message);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Login failed'));
    },
  });
}

export function useRegisterMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: register,
    onSuccess: (response) => {
      saveAuthTokens(response);
      queryClient.setQueryData(queryKeys.auth, response.user);
      toast.success(response.message);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Register failed'));
    },
  });
}

export function useGoogleLoginMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: loginWithGoogle,
    onSuccess: (response) => {
      saveAuthTokens(response);
      queryClient.setQueryData(queryKeys.auth, response.user);
      toast.success(response.message);
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Google sign-in failed'));
    },
  });
}

export function useLogoutMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      const refreshToken = getRefreshToken();

      if (refreshToken) {
        await logout(refreshToken);
      }
    },
    onSettled: () => {
      clearAuthTokens();
      queryClient.removeQueries({ queryKey: queryKeys.auth });
      toast.success('Logged out');
    },
  });
}
