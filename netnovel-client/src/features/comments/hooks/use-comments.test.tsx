import { act, renderHook, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { queryKeys } from '@/config/query-keys';
import { createQueryWrapper, createTestQueryClient } from '@/test/query-client';

import {
  useCommentContext,
  useCommentReplies,
  useComments,
  useCreateCommentMutation,
  useCreateCommentReplyMutation,
  useDeleteCommentMutation,
  useUpdateCommentMutation,
} from './use-comments';

vi.mock('sonner', () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

vi.mock('../api/comment-api', () => ({
  createComment: vi.fn(),
  createCommentReply: vi.fn(),
  deleteComment: vi.fn(),
  getCommentContext: vi.fn(),
  getCommentReplies: vi.fn(),
  getComments: vi.fn(),
  moderateDeleteComment: vi.fn(),
  updateComment: vi.fn(),
}));

import {
  createComment,
  createCommentReply,
  deleteComment,
  getCommentContext,
  getCommentReplies,
  getComments,
  moderateDeleteComment,
  updateComment,
} from '../api/comment-api';

const mockedCreateComment = vi.mocked(createComment);
const mockedCreateCommentReply = vi.mocked(createCommentReply);
const mockedDeleteComment = vi.mocked(deleteComment);
const mockedGetCommentContext = vi.mocked(getCommentContext);
const mockedGetCommentReplies = vi.mocked(getCommentReplies);
const mockedGetComments = vi.mocked(getComments);
const mockedModerateDeleteComment = vi.mocked(moderateDeleteComment);
const mockedUpdateComment = vi.mocked(updateComment);

describe('use-comments hooks', () => {
  // React Query hook contract: target-based queries, enabled flags, mutation API calls, and comments-cache invalidation.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetches comments for a target with list params', async () => {
    const target = { type: 'novel' as const, id: '10' };
    const params = { page: 1, size: 5 };
    const page = commentPage();
    mockedGetComments.mockResolvedValue(page);

    const { result } = renderHook(() => useComments(target, params), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual(page));
    expect(mockedGetComments).toHaveBeenCalledWith(target, params);
  });

  it('does not fetch comments when target id is blank', () => {
    renderHook(() => useComments({ type: 'novel', id: '' }, { page: 0 }), { wrapper: createQueryWrapper() });

    expect(mockedGetComments).not.toHaveBeenCalled();
  });

  it('respects enabled flag for replies', async () => {
    renderHook(() => useCommentReplies('100', false), { wrapper: createQueryWrapper() });

    expect(mockedGetCommentReplies).not.toHaveBeenCalled();

    mockedGetCommentReplies.mockResolvedValue([comment()]);
    const { result } = renderHook(() => useCommentReplies('100', true), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual([comment()]));
    expect(mockedGetCommentReplies).toHaveBeenCalledWith('100');
  });

  it('fetches comment context only when comment id exists', async () => {
    renderHook(() => useCommentContext(undefined), { wrapper: createQueryWrapper() });
    expect(mockedGetCommentContext).not.toHaveBeenCalled();

    mockedGetCommentContext.mockResolvedValue([comment()]);
    const { result } = renderHook(() => useCommentContext('100'), { wrapper: createQueryWrapper() });

    await waitFor(() => expect(result.current.data).toEqual([comment()]));
    expect(mockedGetCommentContext).toHaveBeenCalledWith('100');
  });

  it('create comment mutation posts payload and invalidates comments cache', async () => {
    const queryClient = createTestQueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const target = { type: 'chapter' as const, id: '5' };
    mockedCreateComment.mockResolvedValue(comment());

    const { result } = renderHook(() => useCreateCommentMutation(target), {
      wrapper: createQueryWrapper(queryClient),
    });

    await act(async () => {
      await result.current.mutateAsync({ content: 'Nice chapter.' });
    });

    expect(mockedCreateComment).toHaveBeenCalledWith(target, { content: 'Nice chapter.' });
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: queryKeys.comments });
  });

  it('reply, update, and delete mutations call the correct API and invalidate comments cache', async () => {
    const queryClient = createTestQueryClient();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    mockedCreateCommentReply.mockResolvedValue(comment());
    mockedUpdateComment.mockResolvedValue(comment());
    mockedDeleteComment.mockResolvedValue(comment());
    mockedModerateDeleteComment.mockResolvedValue(comment());

    const replyHook = renderHook(() => useCreateCommentReplyMutation('100'), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await replyHook.result.current.mutateAsync({ content: 'Reply' });
    });
    expect(mockedCreateCommentReply).toHaveBeenCalledWith('100', { content: 'Reply' });

    const updateHook = renderHook(() => useUpdateCommentMutation(), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await updateHook.result.current.mutateAsync({ commentId: '100', payload: { content: 'Updated' } });
    });
    expect(mockedUpdateComment).toHaveBeenCalledWith('100', { content: 'Updated' });

    const deleteHook = renderHook(() => useDeleteCommentMutation(), {
      wrapper: createQueryWrapper(queryClient),
    });
    await act(async () => {
      await deleteHook.result.current.mutateAsync({ commentId: '100' });
      await deleteHook.result.current.mutateAsync({ commentId: '101', moderate: true });
    });
    expect(mockedDeleteComment).toHaveBeenCalledWith('100');
    expect(mockedModerateDeleteComment).toHaveBeenCalledWith('101');
    expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: queryKeys.comments });
  });

  function comment() {
    return {
      commentId: 100,
      novelId: 10,
      userId: 7,
      username: 'reader',
      content: 'Nice chapter.',
      deleted: false,
      replyCount: 0,
    };
  }

  function commentPage() {
    return {
      content: [comment()],
      number: 1,
      size: 5,
      totalElements: 1,
      totalPages: 1,
      first: false,
      last: true,
    };
  }
});
