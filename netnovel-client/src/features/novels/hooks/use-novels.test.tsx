import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { queryKeys } from '@/config/query-keys';
import { createQueryWrapper, createTestQueryClient } from '@/test/query-client';
import { hasAuthTokens } from '@/features/auth/lib/auth-storage';

import {
  useCreateNovelMutation,
  useDeleteNovelMutation,
  useGenres,
  useIncreaseNovelViewMutation,
  useMyNovelInteraction,
  useNovel,
  useNovelList,
  useNovelTags,
  useSimilarNovels,
  useTags,
  useToggleNovelBookmarkMutation,
  useToggleNovelFollowMutation,
  useToggleNovelLikeMutation,
  useUpdateNovelMutation,
} from './use-novels';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('@/features/auth/lib/auth-storage', () => ({
  hasAuthTokens: vi.fn(),
}));

vi.mock('../api/novel-api', () => ({
  createNovel: vi.fn(),
  deleteNovel: vi.fn(),
  getGenres: vi.fn(),
  getHybridSimilarNovels: vi.fn(),
  getMyNovelInteraction: vi.fn(),
  getNovel: vi.fn(),
  getNovelList: vi.fn(),
  getNovelTags: vi.fn(),
  getSemanticSimilarNovels: vi.fn(),
  getSimilarNovels: vi.fn(),
  getTags: vi.fn(),
  increaseNovelView: vi.fn(),
  recordNovelView: vi.fn(),
  toggleNovelBookmark: vi.fn(),
  toggleNovelFollow: vi.fn(),
  toggleNovelLike: vi.fn(),
  updateNovel: vi.fn(),
}));

import {
  createNovel,
  deleteNovel,
  getGenres,
  getMyNovelInteraction,
  getNovel,
  getNovelList,
  getNovelTags,
  getSimilarNovels,
  getTags,
  increaseNovelView,
  toggleNovelBookmark,
  toggleNovelFollow,
  toggleNovelLike,
  updateNovel,
} from '../api/novel-api';

const mockedCreateNovel = vi.mocked(createNovel);
const mockedDeleteNovel = vi.mocked(deleteNovel);
const mockedGetGenres = vi.mocked(getGenres);
const mockedGetMyNovelInteraction = vi.mocked(getMyNovelInteraction);
const mockedGetNovel = vi.mocked(getNovel);
const mockedGetNovelList = vi.mocked(getNovelList);
const mockedGetNovelTags = vi.mocked(getNovelTags);
const mockedGetSimilarNovels = vi.mocked(getSimilarNovels);
const mockedGetTags = vi.mocked(getTags);
const mockedHasAuthTokens = vi.mocked(hasAuthTokens);
const mockedIncreaseNovelView = vi.mocked(increaseNovelView);
const mockedToggleNovelBookmark = vi.mocked(toggleNovelBookmark);
const mockedToggleNovelFollow = vi.mocked(toggleNovelFollow);
const mockedToggleNovelLike = vi.mocked(toggleNovelLike);
const mockedUpdateNovel = vi.mocked(updateNovel);

describe('use-novels hooks', () => {
  // React Query hook contract:
  // - queries call the correct API and obey enabled guards
  // - interaction mutations merge counter fields back into the novel detail cache
  // - create/update/delete mutations invalidate or remove the expected novel caches

  beforeEach(() => {
    vi.clearAllMocks();
    mockedHasAuthTokens.mockReturnValue(false);
  });

  it('does not fetch a novel when novelId is missing', () => {
    renderHook(() => useNovel(undefined), { wrapper: createQueryWrapper() });

    expect(mockedGetNovel).not.toHaveBeenCalled();
  });

  it('fetches a novel by id', async () => {
    mockedGetNovel.mockResolvedValue(novel());

    const { result } = renderHook(() => useNovel('10'), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual(novel()));
    expect(mockedGetNovel).toHaveBeenCalledWith('10');
  });

  it('fetches novel list, tags, genres, novel tags, and similar novels', async () => {
    const page = novelPage();
    const tags = [{ tagId: 1, name: 'Cultivation' }];
    const genres = [{ genreId: 2, name: 'Fantasy' }];

    mockedGetNovelList.mockResolvedValue(page);
    mockedGetTags.mockResolvedValue(tags);
    mockedGetGenres.mockResolvedValue(genres);
    mockedGetNovelTags.mockResolvedValue(tags);
    mockedGetSimilarNovels.mockResolvedValue(page);

    const listHook = renderHook(() => useNovelList({ kind: 'completed', page: 1, size: 5 }), {
      wrapper: createQueryWrapper(),
    });
    await waitFor(() => expect(listHook.result.current.data).toEqual(page));
    expect(mockedGetNovelList).toHaveBeenCalledWith({ kind: 'completed', page: 1, size: 5 });

    const tagsHook = renderHook(() => useTags(), { wrapper: createQueryWrapper() });
    await waitFor(() => expect(tagsHook.result.current.data).toEqual(tags));
    expect(mockedGetTags).toHaveBeenCalled();

    const genresHook = renderHook(() => useGenres(), { wrapper: createQueryWrapper() });
    await waitFor(() => expect(genresHook.result.current.data).toEqual(genres));
    expect(mockedGetGenres).toHaveBeenCalled();

    const novelTagsHook = renderHook(() => useNovelTags('10'), { wrapper: createQueryWrapper() });
    await waitFor(() => expect(novelTagsHook.result.current.data).toEqual(tags));
    expect(mockedGetNovelTags).toHaveBeenCalledWith('10');

    const similarHook = renderHook(() => useSimilarNovels('10'), { wrapper: createQueryWrapper() });
    await waitFor(() => expect(similarHook.result.current.data).toEqual(page));
    expect(mockedGetSimilarNovels).toHaveBeenCalledWith('10');
  });

  it('does not fetch guarded data when ids or auth tokens are missing', () => {
    renderHook(() => useNovelTags(undefined), { wrapper: createQueryWrapper() });
    renderHook(() => useSimilarNovels(undefined), { wrapper: createQueryWrapper() });
    renderHook(() => useMyNovelInteraction('10'), { wrapper: createQueryWrapper() });

    expect(mockedGetNovelTags).not.toHaveBeenCalled();
    expect(mockedGetSimilarNovels).not.toHaveBeenCalled();
    expect(mockedGetMyNovelInteraction).not.toHaveBeenCalled();
  });

  it('fetches my novel interaction only when auth tokens exist', async () => {
    mockedHasAuthTokens.mockReturnValue(true);
    mockedGetMyNovelInteraction.mockResolvedValue(interaction());

    const { result } = renderHook(() => useMyNovelInteraction('10'), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual(interaction()));
    expect(mockedGetMyNovelInteraction).toHaveBeenCalledWith('10');
  });

  it('interaction mutations update novel detail and interaction caches', async () => {
    const queryClient = createTestQueryClient();
    queryClient.setQueryData([...queryKeys.novels, '10'], novel());
    const nextInteraction = interaction({ views: 20, follows: 4, likes: 5, bookmarks: 6 });

    mockedIncreaseNovelView.mockResolvedValue(nextInteraction);
    mockedToggleNovelFollow.mockResolvedValue(nextInteraction);
    mockedToggleNovelLike.mockResolvedValue(nextInteraction);
    mockedToggleNovelBookmark.mockResolvedValue(nextInteraction);

    const viewHook = renderHook(() => useIncreaseNovelViewMutation('10'), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await viewHook.result.current.mutateAsync('99');
    });
    expect(mockedIncreaseNovelView).toHaveBeenCalledWith('10', '99');

    const followHook = renderHook(() => useToggleNovelFollowMutation('10'), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await followHook.result.current.mutateAsync();
    });
    expect(mockedToggleNovelFollow).toHaveBeenCalledWith('10');

    const likeHook = renderHook(() => useToggleNovelLikeMutation('10'), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await likeHook.result.current.mutateAsync();
    });
    expect(mockedToggleNovelLike).toHaveBeenCalledWith('10');

    const bookmarkHook = renderHook(() => useToggleNovelBookmarkMutation('10'), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await bookmarkHook.result.current.mutateAsync();
    });
    expect(mockedToggleNovelBookmark).toHaveBeenCalledWith('10');

    expect(queryClient.getQueryData([...queryKeys.novels, '10'])).toMatchObject({
      views: 20,
      follows: 4,
      likes: 5,
      bookmarks: 6,
    });
    expect(queryClient.getQueryData([...queryKeys.novels, '10', 'interaction'])).toEqual(nextInteraction);
  });

  it('create, update, and delete mutations maintain novel caches', async () => {
    const queryClient = createTestQueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const removeSpy = vi.spyOn(queryClient, 'removeQueries');

    mockedCreateNovel.mockResolvedValue(novel({ novelId: 11, title: 'Created Novel' }));
    mockedUpdateNovel.mockResolvedValue(novel({ title: 'Updated Novel' }));
    mockedDeleteNovel.mockResolvedValue(undefined);

    const createHook = renderHook(() => useCreateNovelMutation(), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await createHook.result.current.mutateAsync(payload());
    });
    expect(mockedCreateNovel.mock.calls[0][0]).toEqual(payload());
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: queryKeys.novels });

    const updateHook = renderHook(() => useUpdateNovelMutation('10'), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await updateHook.result.current.mutateAsync(payload({ title: 'Updated Novel' }));
    });
    expect(mockedUpdateNovel).toHaveBeenCalledWith('10', payload({ title: 'Updated Novel' }));
    expect(queryClient.getQueryData([...queryKeys.novels, '10'])).toEqual(novel({ title: 'Updated Novel' }));
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: queryKeys.novels });

    const deleteHook = renderHook(() => useDeleteNovelMutation('10'), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await deleteHook.result.current.mutateAsync();
    });
    expect(mockedDeleteNovel).toHaveBeenCalledWith('10');
    expect(removeSpy).toHaveBeenCalledWith({ queryKey: [...queryKeys.novels, '10'] });
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: queryKeys.novels });
  });

  function payload(overrides = {}) {
    return {
      title: 'NetNovel Sample',
      author: 'Author',
      description: 'Long enough description.',
      coverImageUrl: 'https://example.test/cover.jpg',
      genres: ['Fantasy'],
      tags: ['Cultivation'],
      status: 'ONGOING' as const,
      accessStatus: 'NORMAL' as const,
      ...overrides,
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

  function novelPage() {
    return {
      content: [novel()],
      number: 1,
      size: 5,
      totalElements: 1,
      totalPages: 1,
      first: false,
      last: true,
    };
  }

  function interaction(overrides = {}) {
    return {
      novelId: 10,
      followed: true,
      liked: true,
      bookmarked: true,
      views: 12,
      follows: 2,
      likes: 3,
      bookmarks: 1,
      ...overrides,
    };
  }
});
