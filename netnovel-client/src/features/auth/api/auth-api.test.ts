import { beforeEach, describe, expect, it, vi } from 'vitest';

import { httpClient } from '@/lib/api/http-client';

import { getCurrentUser, login, loginWithGoogle, logout, register } from './auth-api';

vi.mock('@/lib/api/http-client', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

const mockedHttpClient = vi.mocked(httpClient);

describe('auth-api', () => {
  // API contract tests: verify endpoint, HTTP verb, payload, and returned response.data.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('posts login credentials and returns auth response data', async () => {
    const payload = { email: 'reader@example.com', password: 'secret' };
    const data = { accessToken: 'access', refreshToken: 'refresh', message: 'ok', user: user() };
    mockedHttpClient.post.mockResolvedValue({ data });

    await expect(login(payload)).resolves.toBe(data);

    expect(mockedHttpClient.post).toHaveBeenCalledWith('/auth/login', payload);
  });

  it('posts register payload to the register endpoint', async () => {
    const payload = { username: 'reader', email: 'reader@example.com', password: 'secret' };
    const data = { accessToken: 'access', refreshToken: 'refresh', message: 'ok', user: user() };
    mockedHttpClient.post.mockResolvedValue({ data });

    await expect(register(payload)).resolves.toBe(data);

    expect(mockedHttpClient.post).toHaveBeenCalledWith('/auth/register', payload);
  });

  it('posts google login payload to the google endpoint', async () => {
    const payload = { idToken: 'google-id-token' };
    const data = { accessToken: 'access', refreshToken: 'refresh', message: 'ok', user: user() };
    mockedHttpClient.post.mockResolvedValue({ data });

    await expect(loginWithGoogle(payload)).resolves.toBe(data);

    expect(mockedHttpClient.post).toHaveBeenCalledWith('/auth/google', payload);
  });

  it('gets the current user from the me endpoint', async () => {
    const data = user();
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getCurrentUser()).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/auth/me');
  });

  it('posts refresh token when logging out', async () => {
    mockedHttpClient.post.mockResolvedValue({ data: undefined });

    await logout('refresh-token');

    expect(mockedHttpClient.post).toHaveBeenCalledWith('/auth/logout', { refreshToken: 'refresh-token' });
  });

  function user() {
    return {
      userId: 1,
      username: 'reader',
      email: 'reader@example.com',
    };
  }
});
