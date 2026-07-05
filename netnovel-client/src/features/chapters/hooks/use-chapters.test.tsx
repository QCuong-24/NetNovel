import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { queryKeys } from '@/config/query-keys';
import { createQueryWrapper, createTestQueryClient } from '@/test/query-client';

import {
  useChapter,
  useCreateChapterMutation,
  useNovelChapters,
  useUpdateChapterMutation,
} from './use-chapters';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('../api/chapter-api', () => ({
  createChapter: vi.fn(),
  deleteChapter: vi.fn(),
  getChapter: vi.fn(),
  getNovelChapters: vi.fn(),
  updateChapter: vi.fn(),
}));

import { createChapter, getChapter, getNovelChapters, updateChapter } from '../api/chapter-api';

const mockedCreateChapter = vi.mocked(createChapter);
const mockedGetChapter = vi.mocked(getChapter);
const mockedGetNovelChapters = vi.mocked(getNovelChapters);
const mockedUpdateChapter = vi.mocked(updateChapter);

describe('use-chapters hooks', () => {
  // React Query hook contract: query enablement, mutation calls, cache writes, and invalidation targets.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('does not fetch a chapter when chapterId is missing', () => {
    renderHook(() => useChapter(undefined), { wrapper: createQueryWrapper() });

    expect(mockedGetChapter).not.toHaveBeenCalled();
  });

  it('fetches a chapter by id', async () => {
    mockedGetChapter.mockResolvedValue(chapter());

    const { result } = renderHook(() => useChapter('5'), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual(chapter()));
    expect(mockedGetChapter).toHaveBeenCalledWith('5');
  });

  it('fetches all chapters for a novel', async () => {
    const chapters = [chapter()];
    mockedGetNovelChapters.mockResolvedValue(chapters);

    const { result } = renderHook(() => useNovelChapters('10'), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual(chapters));
    expect(mockedGetNovelChapters).toHaveBeenCalledWith('10');
  });

  it('create mutation calls API and invalidates the novel chapter list', async () => {
    const queryClient = createTestQueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    mockedCreateChapter.mockResolvedValue(chapter());

    const { result } = renderHook(() => useCreateChapterMutation('10'), {
      wrapper: createQueryWrapper(queryClient),
    });

    await act(async () => {
      await result.current.mutateAsync(payload());
    });

    expect(mockedCreateChapter).toHaveBeenCalledWith('10', payload());
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: [...queryKeys.chapters, 'novel', '10'] });
  });

  it('update mutation writes chapter cache and invalidates parent novel chapter list', async () => {
    const queryClient = createTestQueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    mockedUpdateChapter.mockResolvedValue(chapter());

    const { result } = renderHook(() => useUpdateChapterMutation('5'), {
      wrapper: createQueryWrapper(queryClient),
    });

    await act(async () => {
      await result.current.mutateAsync(payload());
    });

    expect(mockedUpdateChapter).toHaveBeenCalledWith('5', payload());
    expect(queryClient.getQueryData([...queryKeys.chapters, '5'])).toEqual(chapter());
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: [...queryKeys.chapters, 'novel', '10'] });
  });

  function payload() {
    return {
      title: 'Chapter One',
      chapterNumber: 1,
      content: 'Long chapter content.',
    };
  }

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
