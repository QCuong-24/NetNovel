import type { AuthResponse } from '../types';

export const accessTokenStorageKey = 'netnovel-access-token';
export const refreshTokenStorageKey = 'netnovel-refresh-token';

export function getAccessToken() {
  return window.localStorage.getItem(accessTokenStorageKey);
}

export function getRefreshToken() {
  return window.localStorage.getItem(refreshTokenStorageKey);
}

export function hasAuthTokens() {
  return Boolean(getAccessToken() && getRefreshToken());
}

export function saveAuthTokens(response: AuthResponse) {
  window.localStorage.setItem(accessTokenStorageKey, response.accessToken);
  window.localStorage.setItem(refreshTokenStorageKey, response.refreshToken);
}

export function clearAuthTokens() {
  window.localStorage.removeItem(accessTokenStorageKey);
  window.localStorage.removeItem(refreshTokenStorageKey);
}
