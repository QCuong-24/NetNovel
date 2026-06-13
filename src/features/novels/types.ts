export type NovelStatus = 'ONGOING' | 'COMPLETED';

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
  tags: string[];
  status: NovelStatus;
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
  tags: string[];
  status: NovelStatus;
};

export type Tag = {
  tagId: number;
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

export type NovelListKind = 'all' | 'newest' | 'hot' | 'completed' | 'tag';

export type NovelListParams = {
  kind: NovelListKind;
  tagName?: string;
  page?: number;
  size?: number;
};

export type NovelSearchResult = {
  novel: Novel;
  score?: number | null;
};
