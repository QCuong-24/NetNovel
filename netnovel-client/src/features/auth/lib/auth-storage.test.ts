import { beforeEach, describe, expect, it } from 'vitest';

import type { AuthResponse } from '../types';

import {
  accessTokenStorageKey,
  clearAuthTokens,
  getAccessToken,
  getRefreshToken,
  hasAuthTokens,
  refreshTokenStorageKey,
  saveAuthTokens,
} from './auth-storage';

describe('auth storage', () => {
  // Auth state is token-based: both access and refresh tokens must be present.

  beforeEach(() => {
    window.localStorage.clear();
  });

  it('saves and reads access and refresh tokens', () => {
    saveAuthTokens(authResponse('access-token', 'refresh-token'));

    expect(getAccessToken()).toBe('access-token');
    expect(getRefreshToken()).toBe('refresh-token');
    expect(window.localStorage.getItem(accessTokenStorageKey)).toBe('access-token');
    expect(window.localStorage.getItem(refreshTokenStorageKey)).toBe('refresh-token');
  });

  it('requires both tokens to consider the user authenticated', () => {
    expect(hasAuthTokens()).toBe(false);

    window.localStorage.setItem(accessTokenStorageKey, 'access-token');
    expect(hasAuthTokens()).toBe(false);

    window.localStorage.setItem(refreshTokenStorageKey, 'refresh-token');
    expect(hasAuthTokens()).toBe(true);
  });

  it('clears both auth tokens', () => {
    saveAuthTokens(authResponse('access-token', 'refresh-token'));

    clearAuthTokens();

    expect(getAccessToken()).toBeNull();
    expect(getRefreshToken()).toBeNull();
  });

  function authResponse(accessToken: string, refreshToken: string): AuthResponse {
    return {
      accessToken,
      refreshToken,
      message: 'ok',
      user: {
        userId: 1,
        username: 'reader',
        email: 'reader@example.com',
      },
    };
  }
});
