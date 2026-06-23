import type { Novel } from '@/features/novels/types';

export type RecommendationReason = 'BASED_ON_CONTENT' | 'BECAUSE_YOU_READ_SIMILAR' | 'TRENDING';

export type RecommendationItem = {
  novel: Novel;
  score: number;
  reason: RecommendationReason;
};
