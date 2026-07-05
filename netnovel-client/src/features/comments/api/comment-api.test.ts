import { beforeEach, describe, expect, it, vi } from 'vitest';

import { httpClient } from '@/lib/api/http-client';

import {
  createComment,
  createCommentReply,
  deleteComment,
  getCommentContext,
  getCommentReplies,
  getComments,
  moderateDeleteComment,
  updateComment,
} from './comment-api';

vi.mock('@/lib/api/http-client', () => ({
  httpClient: {
    delete: vi.fn(),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}));

const mockedHttpClient = vi.mocked(httpClient);

describe('comment-api', () => {
  // API contract tests for comment target routing, pagination query strings, and mutation payloads.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('gets novel comments with default pagination', async () => {
    const data = page();
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getComments({ type: 'novel', id: '10' })).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/novels/10/comments?page=0&size=10');
  });

  it('gets chapter comments with explicit pagination and sort', async () => {
    const data = page();
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getComments({ type: 'chapter', id: '5' }, { page: 2, size: 20, sort: 'createdAt,desc' })).resolves.toBe(data);

    expect(mockedHttpClient.get).toHaveBeenCalledWith('/chapters/5/comments?page=2&size=20&sort=createdAt%2Cdesc');
  });

  it('gets replies and context by comment id', async () => {
    const data = [comment()];
    mockedHttpClient.get.mockResolvedValue({ data });

    await expect(getCommentReplies('100')).resolves.toBe(data);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/comments/100/replies');

    await expect(getCommentContext('100')).resolves.toBe(data);
    expect(mockedHttpClient.get).toHaveBeenCalledWith('/comments/100/context');
  });

  it('creates comments for novels and replies to comments', async () => {
    const payload = { content: 'Nice chapter.' };
    const data = comment();
    mockedHttpClient.post.mockResolvedValue({ data });

    await expect(createComment({ type: 'novel', id: '10' }, payload)).resolves.toBe(data);
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/novels/10/comments', payload);

    await expect(createCommentReply('100', payload)).resolves.toBe(data);
    expect(mockedHttpClient.post).toHaveBeenCalledWith('/comments/100/replies', payload);
  });

  it('updates and deletes comments by id', async () => {
    const payload = { content: 'Updated comment.' };
    const data = comment();
    mockedHttpClient.put.mockResolvedValue({ data });
    mockedHttpClient.delete.mockResolvedValue({ data });

    await expect(updateComment('100', payload)).resolves.toBe(data);
    expect(mockedHttpClient.put).toHaveBeenCalledWith('/comments/100', payload);

    await expect(deleteComment('100')).resolves.toBe(data);
    expect(mockedHttpClient.delete).toHaveBeenCalledWith('/comments/100');

    await expect(moderateDeleteComment('100')).resolves.toBe(data);
    expect(mockedHttpClient.delete).toHaveBeenCalledWith('/comments/100/moderation');
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

  function page() {
    return {
      content: [comment()],
      number: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    };
  }
});
