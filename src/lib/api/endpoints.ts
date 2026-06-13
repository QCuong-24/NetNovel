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
    delete: (novelId: string) => `/novels/${novelId}`,
    view: (novelId: string) => `/novels/${novelId}/view`,
    myInteraction: (novelId: string) => `/novels/${novelId}/me`,
    toggleFollow: (novelId: string) => `/novels/${novelId}/follow/toggle`,
    toggleLike: (novelId: string) => `/novels/${novelId}/like/toggle`,
    latest: '/novels/latest-updates',
    completed: '/novels/completed',
    coverSignature: (novelId: string) => `/novels/${novelId}/cover/upload-signature`,
    cover: (novelId: string) => `/novels/${novelId}/cover`,
  },
  search: {
    novels: '/search/novels',
  },
  advancedSearch: {
    novels: '/advanced/search/novels',
    reindexNovels: '/advanced/search/reindex/novels',
  },
  recommendations: {
    similarNovels: (novelId: string) => `/recommendations/novels/${novelId}/similar`,
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
    delete: (chapterId: string) => `/chapters/${chapterId}`,
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
  crawlTasks: {
    list: '/crawl-tasks',
    create: '/crawl-tasks',
    detail: (taskId: string) => `/crawl-tasks/${taskId}`,
    chapterRecords: '/crawl-tasks/crawl-chapter-records',
    chapterRecord: (recordId: string) => `/crawl-tasks/crawl-chapter-records/${recordId}`,
  },
} as const;
