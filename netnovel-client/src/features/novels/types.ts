export type NovelStatus = 'ONGOING' | 'COMPLETED';
export type NovelAccessStatus = 'NORMAL' | 'PREVIEW_ONLY';

export type Novel = {
  novelId: number;
  title: string;
  author: string;
  description: string;
  coverImageUrl?: string | null;
  coverImagePublicId?: string | null;
  views: number;
  follows: number;
  likes: number;
  bookmarks: number;
  genres: string[];
  status: NovelStatus;
  accessStatus: NovelAccessStatus;
  chapterCount: number;
  latestChapterId?: number | null;
  latestChapterNumber?: number | null;
  latestChapterTitle?: string | null;
  latestChapterUpdatedAt?: string | null;
  createAt?: string;
  updateAt?: string;
};

export type NovelPayload = {
  title: string;
  author: string;
  description: string;
  coverImageUrl: string;
  genres: string[];
  tags: string[];
  status: NovelStatus;
  accessStatus?: NovelAccessStatus;
};

export type Tag = {
  tagId: number;
  name: string;
};

export type Genre = {
  genreId: number;
  name: string;
};

export type PageResponse<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type NovelListKind = 'all' | 'newest' | 'hot' | 'completed' | 'genre';

export type NovelListParams = {
  kind: NovelListKind;
  genreName?: string;
  page?: number;
  size?: number;
};

export type NovelSearchResult = {
  novel: Novel;
  score?: number | null;
};

export type SimilarNovelRecommendation = {
  novel: Novel;
  score?: number | null;
  semanticScore?: number | null;
  contentScore?: number | null;
  popularityScore?: number | null;
  reasons: string[];
};

export type NovelInteraction = {
  novelId: number;
  followed: boolean;
  liked: boolean;
  bookmarked: boolean;
  views: number;
  follows: number;
  likes: number;
  bookmarks: number;
};
