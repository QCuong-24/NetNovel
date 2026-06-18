export type ChapterSummary = {
  chapterId: number;
  novelId: number;
  novelTitle: string;
  title: string;
  chapterNumber: number;
  updateAt?: string;
};

export type ChapterContent = ChapterSummary & {
  content: string;
};

export type ChapterPayload = {
  title: string;
  chapterNumber: number;
  content: string;
};
