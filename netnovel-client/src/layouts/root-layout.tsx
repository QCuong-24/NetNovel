import { Outlet } from 'react-router-dom';
import { AppHeader } from '@/components/layout/app-header';
import { BackToTopButton } from '@/components/layout/back-to-top-button';
import { AppFooter } from '@/components/layout/app-footer';
import { RouteHistoryTracker } from '@/components/layout/route-history-tracker';
import { ScrollToTop } from '@/components/layout/scroll-to-top';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { useNotificationStream } from '@/features/notifications/hooks/use-notifications';
import { ChatbotWidget } from '@/features/chatbot/components/chatbot-widget';

export function RootLayout() {
  const { data: user } = useCurrentUser();
  useNotificationStream(Boolean(user));

  return (
    <div className="grid min-h-screen grid-rows-[auto_1fr_auto] bg-background text-foreground">
      <RouteHistoryTracker />
      <ScrollToTop />
      <AppHeader />
      <Outlet />
      <AppFooter />
      <BackToTopButton />
      <ChatbotWidget />
    </div>
  );
}
