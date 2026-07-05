import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { PropsWithChildren } from 'react';

export function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      mutations: {
        retry: false,
      },
      queries: {
        retry: false,
      },
    },
  });
}

export function createQueryWrapper(queryClient = createTestQueryClient()) {
  return function QueryWrapper({ children }: PropsWithChildren) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}
