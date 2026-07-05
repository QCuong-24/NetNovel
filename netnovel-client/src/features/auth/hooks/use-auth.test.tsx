import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { queryKeys } from '@/config/query-keys';
import { createQueryWrapper, createTestQueryClient } from '@/test/query-client';

import { useCurrentUser, useLoginMutation, useLogoutMutation } from './use-auth';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('../api/auth-api', () => ({
  getCurrentUser: vi.fn(),
  login: vi.fn(),
  logout: vi.fn(),
}));

vi.mock('../lib/auth-storage', () => ({
  clearAuthTokens: vi.fn(),
  getRefreshToken: vi.fn(),
  hasAuthTokens: vi.fn(),
  saveAuthTokens: vi.fn(),
}));

import { getCurrentUser, login, logout } from '../api/auth-api';
import { clearAuthTokens, getRefreshToken, hasAuthTokens, saveAuthTokens } from '../lib/auth-storage';

const mockedGetCurrentUser = vi.mocked(getCurrentUser);
const mockedLogin = vi.mocked(login);
const mockedLogout = vi.mocked(logout);
const mockedClearAuthTokens = vi.mocked(clearAuthTokens);
const mockedGetRefreshToken = vi.mocked(getRefreshToken);
const mockedHasAuthTokens = vi.mocked(hasAuthTokens);
const mockedSaveAuthTokens = vi.mocked(saveAuthTokens);

describe('use-auth hooks', () => {
  // React Query hook contract: query enablement, cache updates, token storage side effects, and logout cleanup.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('does not fetch current user when auth tokens are missing', () => {
    mockedHasAuthTokens.mockReturnValue(false);

    renderHook(() => useCurrentUser(), { wrapper: createQueryWrapper() });

    expect(mockedGetCurrentUser).not.toHaveBeenCalled();
  });

  it('fetches current user when auth tokens exist', async () => {
    mockedHasAuthTokens.mockReturnValue(true);
    mockedGetCurrentUser.mockResolvedValue(user());

    const { result } = renderHook(() => useCurrentUser(), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual(user()));
    expect(mockedGetCurrentUser).toHaveBeenCalledTimes(1);
  });

  it('login mutation stores tokens and writes current user into cache', async () => {
    const queryClient = createTestQueryClient();
    const response = {
      user: user(),
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      message: 'Login successfully',
    };
    mockedLogin.mockResolvedValue(response);

    const { result } = renderHook(() => useLoginMutation(), { wrapper: createQueryWrapper(queryClient) });

    await act(async () => {
      await result.current.mutateAsync({ email: 'reader@example.com', password: 'secret' });
    });

    expect(mockedSaveAuthTokens).toHaveBeenCalledWith(response);
    expect(queryClient.getQueryData(queryKeys.auth)).toEqual(user());
  });

  it('logout mutation revokes refresh token, clears storage, and removes auth cache', async () => {
    const queryClient = createTestQueryClient();
    queryClient.setQueryData(queryKeys.auth, user());
    mockedGetRefreshToken.mockReturnValue('refresh-token');
    mockedLogout.mockResolvedValue(undefined);

    const { result } = renderHook(() => useLogoutMutation(), { wrapper: createQueryWrapper(queryClient) });

    await act(async () => {
      await result.current.mutateAsync();
    });

    expect(mockedLogout).toHaveBeenCalledWith('refresh-token');
    expect(mockedClearAuthTokens).toHaveBeenCalled();
    expect(queryClient.getQueryData(queryKeys.auth)).toBeUndefined();
  });

  function user() {
    return {
      userId: 7,
      username: 'reader',
      email: 'reader@example.com',
    };
  }
});
