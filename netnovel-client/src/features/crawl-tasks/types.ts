export type CrawlTaskStatus =
  | 'PENDING'
  | 'RUNNING'
  | 'SUCCESS'
  | 'PARTIAL_SUCCESS'
  | 'FAILED'
  | 'SKIPPED_UNSUPPORTED_SOURCE'
  | 'CANCELLED';

export type CrawlChapterStatus = 'SUCCESS' | 'FAILED';

export type CrawlTask = {
  id: number;
  url: string;
  status: CrawlTaskStatus;
  requestedByUserId?: number | null;
  errorMessage?: string | null;
  startedAt?: string | null;
  finishedAt?: string | null;
  createAt?: string | null;
  updateAt?: string | null;
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

export type CrawlTaskPage = PageResponse<CrawlTask>;

export type CrawlTaskListParams = {
  status?: CrawlTaskStatus;
  personal?: boolean;
  page?: number;
  size?: number;
};

export type CrawlTaskCreatePayload = {
  url: string;
};

export type CrawlChapterRecord = {
  id: number;
  sourceName: string;
  sourceChapterUrl: string;
  novelId?: number | null;
  novelTitle?: string | null;
  chapterId?: number | null;
  chapterTitle?: string | null;
  chapterNumber?: number | null;
  status: CrawlChapterStatus;
  errorMessage?: string | null;
  crawledAt?: string | null;
};

export type CrawlChapterRecordPage = PageResponse<CrawlChapterRecord>;

export type CrawlChapterRecordListParams = {
  status?: CrawlChapterStatus;
  novelId?: string;
  start?: string;
  end?: string;
  page?: number;
  size?: number;
};
