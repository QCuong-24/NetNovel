import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { env } from '@/config/env';
import { endpoints } from './endpoints';
import {
  accessTokenStorageKey,
  clearAuthTokens,
  getRefreshToken,
  saveAuthTokens,
} from '@/features/auth/lib/auth-storage';
import type { AuthResponse } from '@/features/auth/types';

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

export const httpClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

httpClient.interceptors.request.use((config) => {
  const token = window.localStorage.getItem(accessTokenStorageKey);

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

let refreshTokenRequest: Promise<AuthResponse> | null = null;

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    throw new Error('Missing refresh token');
  }

  refreshTokenRequest ??= axios
    .post<AuthResponse>(`${env.apiBaseUrl}${endpoints.auth.refresh}`, { refreshToken })
    .then((response) => {
      saveAuthTokens(response.data);

      return response.data;
    })
    .finally(() => {
      refreshTokenRequest = null;
    });

  return refreshTokenRequest;
}

httpClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;
    const status = error.response?.status;
    const requestUrl = originalRequest?.url ?? '';
    const isAuthRefreshRequest =
      requestUrl.includes(endpoints.auth.refresh) || requestUrl.includes(endpoints.auth.logout);

    if (!originalRequest || status !== 401 || originalRequest._retry || isAuthRefreshRequest) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const authResponse = await refreshAccessToken();
      originalRequest.headers.Authorization = `Bearer ${authResponse.accessToken}`;

      return httpClient(originalRequest);
    } catch (refreshError) {
      clearAuthTokens();

      return Promise.reject(refreshError);
    }
  },
);
