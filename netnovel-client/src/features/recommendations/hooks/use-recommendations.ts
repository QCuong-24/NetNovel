import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/config/query-keys';
import { hasAuthTokens } from '@/features/auth/lib/auth-storage';
import { getForYouRecommendations } from '../api/recommendation-api';

export function useForYouRecommendations(size = 6, enabled = true) {
  return useQuery({
    queryKey: [...queryKeys.recommendations, 'forYou', size],
    queryFn: () => getForYouRecommendations(size),
    enabled: enabled && hasAuthTokens(),
    staleTime: 5 * 60_000,
  });
}
