export const env = {
  appName: import.meta.env.VITE_APP_NAME ?? 'NetNovel',
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api',
  googleClientId: import.meta.env.VITE_GOOGLE_CLIENT_ID ?? '',
} as const;
