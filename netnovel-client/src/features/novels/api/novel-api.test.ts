import { beforeEach, describe, expect, it, vi } from 'vitest';

import { httpClient } from '@/lib/api/http-client';

import {
  createNovel,
  deleteNovel,
  getGenres,
  getHybridSimilarNovels,
  getMyNovelInteraction,
  getNovel,
  getNovelList,
  getNovelTags,
  getSemanticSimilarNovels,
  getSimilarNovels,
  getTags,
  increaseNovelView,
  recordNovelView,
  toggleNovelBookmark,
  toggleNovelFollow,
  toggleNovelLike,
  updateNovel,
} from './novel-api';

vi.mock('@/lib/api/http-client', () => ({
  httpClient: {
    delete: vi.fn(),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}));

const mockedHttpClient = vi.mocked(httpClient);

describe('novel-api', () => {
  // API contract tests for novel endpoint paths, query-string construction,
  // recommendation response flattening, and interaction mutation routes.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('gets a single novel by id', async () => {
    const data = novel();
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getNovel('10')).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels/10');
  });

  it('gets standard novel lists from the expected endpoints', async () => {
    const data = page([novel()]);
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getNovelList({ kind: 'all' })).resolves.toBe(data);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels?page=0&size=20');

    await expect(getNovelList({ kind: 'newest', page: 1, size: 6 })).resolves.toBe(data);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels/latest-updates?page=1&size=6');

    await expect(getNovelList({ kind: 'completed', page: 2, size: 4 })).resolves.toBe(data);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels/completed?page=2&size=4');
  });

  it('gets search-backed hot and genre lists and unwraps search results', async () => {
    const searchPage = page([{ novel: novel(), score: 0.9 }]);
    mockedHttpClient.get.mockResolvedValue({ data: searchPage });

    await expect(getNovelList({ kind: 'hot', page: 0, size: 8 })).resolves.toEqual(page([novel()]));
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/search/novels?page=0&size=8&sortMode=popular');

    await expect(getNovelList({ kind: 'genre', genreName: 'Fantasy', page: 1, size: 5 })).resolves.toEqual(
      page([novel()]),
    );
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/search/novels?page=1&size=5&sortMode=latest&genre=Fantasy');
  });

  it('gets similar recommendations and flattens wrapped novel results', async () => {
    const searchPage = page([{ novel: novel(), score: 0.8 }]);
    mockedHttpClient.get.mockResolvedValue({ data: searchPage });

    await expect(getSimilarNovels('10')).resolves.toEqual(page([novel()]));
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/recommendations/novels/10/similar?page=0&size=6');

    await expect(getSemanticSimilarNovels('10', 3)).resolves.toEqual(page([novel()]));
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/recommendations/novels/10/similar/semantic?page=0&size=3');
  });

  it('gets hybrid similar recommendations without flattening recommendation metadata', async () => {
    const data = page([
      {
        novel: novel(),
        score: 0.7,
        semanticScore: 0.6,
        contentScore: 0.5,
        popularityScore: 0.4,
        reasons: ['same genre'],
      },
    ]);
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getHybridSimilarNovels('10', 2)).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/recommendations/novels/10/similar/hybrid?page=0&size=2');
  });

  it('creates, updates, and deletes novels through management endpoints', async () => {
    const request = payload();
    const data = novel({ title: 'Updated Novel' });
    mockedHttpClient.post.mockResolvedValue({ data });
    mockedHttpClient.put.mockResolvedValue({ data });
    mockedHttpClient.delete.mockResolvedValue({ data: undefined });

    await expect(createNovel(request)).resolves.toBe(data);
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels', request);

    await expect(updateNovel('10', request)).resolves.toBe(data);
    expect(mockedHttpClient.put).toHaveBeenCalledWith('/novels/10', request);

    await deleteNovel('10');
    expect(mockedHttpClient.delete).toHaveBeenCalledWith('/novels/10');
  });

  it('uses the expected interaction and view endpoints', async () => {
    const data = interaction();
    mockedHttpClient.get.mockResolvedValue({ data });
    mockedHttpClient.post.mockResolvedValue({ data });

    await expect(getMyNovelInteraction('10')).resolves.toBe(data);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels/10/me');

    await expect(increaseNovelView('10', '99')).resolves.toBe(data);
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels/10/view?chapterId=99');

    await recordNovelView('10');
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels/10/view-event');

    await expect(toggleNovelFollow('10')).resolves.toBe(data);
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels/10/follow/toggle');

    await expect(toggleNovelLike('10')).resolves.toBe(data);
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels/10/like/toggle');

    await expect(toggleNovelBookmark('10')).resolves.toBe(data);
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels/10/bookmark/toggle');
  });

  it('gets tags, novel tags, and genres', async () => {
    const tags = [{ tagId: 1, name: 'Cultivation' }];
    const genres = [{ genreId: 2, name: 'Fantasy' }];
    mockedHttpClient.get.mockResolvedValueOnce({ data: tags });
    mockedHttpClient.get.mockResolvedValueOnce({ data: tags });
    mockedHttpClient.get.mockResolvedValueOnce({ data: genres });

    await expect(getTags()).resolves.toBe(tags);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/tags');

    await expect(getNovelTags('10')).resolves.toBe(tags);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/tags/novels/10');

    await expect(getGenres()).resolves.toBe(genres);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/genres');
  });

  function payload() {
    return {
      title: 'NetNovel Sample',
      author: 'Author',
      description: 'Long enough description.',
      coverImageUrl: 'https://example.test/cover.jpg',
      genres: ['Fantasy'],
      tags: ['Cultivation'],
      status: 'ONGOING' as const,
      accessStatus: 'NORMAL' as const,
    };
  }

  function novel(overrides = {}) {
    return {
      novelId: 10,
      title: 'NetNovel Sample',
      author: 'Author',
      description: 'Long enough description.',
      coverImageUrl: 'https://example.test/cover.jpg',
      views: 12,
      follows: 2,
      likes: 3,
      bookmarks: 1,
      genres: ['Fantasy'],
      status: 'ONGOING' as const,
      accessStatus: 'NORMAL' as const,
      chapterCount: 7,
      ...overrides,
    };
  }

  function page<T>(content: T[]) {
    return {
      content,
      number: 0,
      size: 20,
      totalElements: content.length,
      totalPages: 1,
      first: true,
      last: true,
    };
  }

  function interaction() {
    return {
      novelId: 10,
      followed: true,
      liked: true,
      bookmarked: true,
      views: 13,
      follows: 3,
      likes: 4,
      bookmarks: 2,
    };
  }
});
