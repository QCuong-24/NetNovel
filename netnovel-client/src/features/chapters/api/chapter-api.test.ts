import { beforeEach, describe, expect, it, vi } from 'vitest';

import { httpClient } from '@/lib/api/http-client';

import { createChapter, deleteChapter, getChapter, getNovelChapterPage, getNovelChapters, updateChapter } from './chapter-api';

vi.mock('@/lib/api/http-client', () => ({
  httpClient: {
    delete: vi.fn(),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}));

const mockedHttpClient = vi.mocked(httpClient);

describe('chapter-api', () => {
  // API contract tests for chapter endpoints; httpClient is mocked so no network is used.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('gets a single chapter by id', async () => {
    const data = chapter();
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getChapter('5')).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/chapters/5');
  });

  it('gets all chapters for a novel', async () => {
    const data = [chapter()];
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getNovelChapters('10')).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels/10/chapters/all');
  });

  it('gets a paginated chapter list for a novel', async () => {
    const data = {
      content: [chapter()],
      first: true,
      last: false,
      number: 0,
      size: 10,
      totalElements: 25,
      totalPages: 3,
    };
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getNovelChapterPage('10', { page: 1, size: 5 })).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels/10/chapters?page=1&size=5');
  });

  it('creates a chapter with payload', async () => {
    const payload = { title: 'Chapter One', chapterNumber: 1, content: 'Long chapter content.' };
    const data = chapter();
    mockedHttpClient.post.mockResolvedValue({ data });

    await expect(createChapter('10', payload)).resolves.toBe(data);

    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels/10/chapters', payload);
  });

  it('updates a chapter with payload', async () => {
    const payload = { title: 'Updated Chapter', chapterNumber: 2, content: 'Updated chapter content.' };
    const data = { ...chapter(), ...payload };
    mockedHttpClient.put.mockResolvedValue({ data });

    await expect(updateChapter('5', payload)).resolves.toBe(data);

    expect(mockedHttpClient.put).toHaveBeenCalledWith('/chapters/5', payload);
  });

  it('deletes a chapter by id', async () => {
    mockedHttpClient.delete.mockResolvedValue({ data: undefined });

    await deleteChapter('5');

    expect(mockedHttpClient.delete).toHaveBeenCalledWith('/chapters/5');
  });

  function chapter() {
    return {
      chapterId: 5,
      novelId: 10,
      novelTitle: 'Novel',
      title: 'Chapter One',
      chapterNumber: 1,
      content: 'Long chapter content.',
    };
  }
});
