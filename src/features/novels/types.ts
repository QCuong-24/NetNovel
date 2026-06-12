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
