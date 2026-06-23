import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { RecommendationItem } from '../types';

export async function getForYouRecommendations(size = 6) {
  const response = await httpClient.get<RecommendationItem[]>(`${endpoints.recommendations.forYou}?size=${size}`);
  return response.data;
}
