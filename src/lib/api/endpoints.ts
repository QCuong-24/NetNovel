export const endpoints = {
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    google: '/auth/google',
    me: '/auth/me',
    logout: '/auth/logout',
    refresh: '/auth/refresh',
  },
  novels: {
    list: '/novels',
    detail: (novelId: string) => `/novels/${novelId}`,
    create: '/novels',
    update: (novelId: string) => `/novels/${novelId}`,
    coverSignature: (novelId: string) => `/novels/${novelId}/cover/upload-signature`,
    cover: (novelId: string) => `/novels/${novelId}/cover`,
  },
  users: {
    avatarSignature: '/users/me/avatar/upload-signature',
    avatar: '/users/me/avatar',
  },
  tags: {
    list: '/tags',
  },
  chapters: {
    byNovel: (novelId: string) => `/novels/${novelId}/chapters`,
    detail: (chapterId: string) => `/chapters/${chapterId}`,
    update: (chapterId: string) => `/chapters/${chapterId}`,
  },
  rankings: {
    list: '/rankings',
  },
  comments: {
    byNovel: (novelId: string) => `/novels/${novelId}/comments`,
  },
  notifications: {
    list: '/notifications',
    sse: '/notifications/stream',
  },
} as const;
