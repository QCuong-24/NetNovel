import { createBrowserRouter } from 'react-router-dom';
import { RootLayout } from '@/layouts/root-layout';
import { ReaderLayout } from '@/layouts/reader-layout';
import { DashboardLayout } from '@/layouts/dashboard-layout';
import { AuthLayout } from '@/layouts/auth-layout';
import { HomePage } from '@/pages/home-page';
import { NotFoundPage } from '@/pages/not-found-page';
import { PlaceholderPage } from '@/pages/placeholder-page';
import { ChapterReaderPage } from '@/features/reader/pages/chapter-reader-page';
import { LoginPage } from '@/features/auth/pages/login-page';
import { RegisterPage } from '@/features/auth/pages/register-page';
import { ProtectedRoute } from '@/features/auth/components/protected-route';
import { NovelListPage } from '@/features/novels/pages/novel-list-page';
import { NovelDetailPage } from '@/features/novels/pages/novel-detail-page';
import { NovelCreatePage } from '@/features/novels/pages/novel-create-page';
import { ChapterCreatePage } from '@/features/chapters/pages/chapter-create-page';
import { ChapterEditPage } from '@/features/chapters/pages/chapter-edit-page';
import { ProfilePage } from '@/features/users/pages/profile-page';
import { CrawlTasksPage } from '@/features/crawl-tasks/pages/crawl-tasks-page';
import { SearchPage } from '@/features/search/pages/search-page';
import { CollectionPage } from '@/features/collection/pages/collection-page';
import { RankingPage } from '@/features/rankings/pages/ranking-page';
import { DashboardPage } from '@/features/dashboard/pages/dashboard-page';
import { NotificationPage } from '@/features/notifications/pages/notification-page';

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'novels', element: <NovelListPage kind="all" /> },
      { path: 'novels/newest', element: <NovelListPage kind="newest" /> },
      { path: 'novels/hot', element: <NovelListPage kind="hot" /> },
      { path: 'novels/completed', element: <NovelListPage kind="completed" /> },
      { path: 'novels/genres/:genreName', element: <NovelListPage kind="genre" /> },
      { path: 'search', element: <SearchPage /> },
      { path: 'rankings', element: <RankingPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'novels/new', element: <NovelCreatePage /> },
          { path: 'novels/:novelId/chapters/new', element: <ChapterCreatePage /> },
          { path: 'novels/:novelId/chapters/:chapterId/edit', element: <ChapterEditPage /> },
          { path: 'collection', element: <CollectionPage /> },
          { path: 'notifications', element: <NotificationPage /> },
          { path: 'profile', element: <ProfilePage /> },
          { path: 'crawl-tasks', element: <CrawlTasksPage /> },
          {
            path: 'dashboard',
            element: <DashboardLayout />,
            children: [
              { index: true, element: <DashboardPage /> },
              { path: 'novels', element: <PlaceholderPage titleKey="nav.myNovels" /> },
            ],
          },
        ],
      },
      { path: 'novels/:novelId', element: <NovelDetailPage /> },
    ],
  },
  {
    element: <ReaderLayout />,
    children: [
      {
        path: 'novels/:novelId/chapters/:chapterId',
        element: <ChapterReaderPage />,
      },
    ],
  },
  {
    element: <AuthLayout />,
    children: [
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
]);
