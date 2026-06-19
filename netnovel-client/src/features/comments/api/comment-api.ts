import { endpoints } from '@/lib/api/endpoints';
import { httpClient } from '@/lib/api/http-client';
import type { Comment, CommentPage, CommentPayload, CommentTarget } from '../types';

type CommentListParams = {
  page?: number;
  size?: number;
  sort?: string;
};

function withPageParams(endpoint: string, params: CommentListParams) {
  const searchParams = new URLSearchParams();
  searchParams.set('page', String(params.page ?? 0));
  searchParams.set('size', String(params.size ?? 10));
  if (params.sort) {
    searchParams.set('sort', params.sort);
  }

  return `${endpoint}?${searchParams.toString()}`;
}

function targetEndpoint(target: CommentTarget) {
  return target.type === 'novel' ? endpoints.comments.byNovel(target.id) : endpoints.comments.byChapter(target.id);
}

export async function getComments(target: CommentTarget, params: CommentListParams = {}) {
  const response = await httpClient.get<CommentPage>(withPageParams(targetEndpoint(target), params));

  return response.data;
}

export async function getCommentReplies(commentId: string) {
  const response = await httpClient.get<Comment[]>(endpoints.comments.replies(commentId));

  return response.data;
}

export async function createComment(target: CommentTarget, payload: CommentPayload) {
  const response = await httpClient.post<Comment>(targetEndpoint(target), payload);

  return response.data;
}

export async function createCommentReply(commentId: string, payload: CommentPayload) {
  const response = await httpClient.post<Comment>(endpoints.comments.replies(commentId), payload);

  return response.data;
}

export async function updateComment(commentId: string, payload: CommentPayload) {
  const response = await httpClient.put<Comment>(endpoints.comments.detail(commentId), payload);

  return response.data;
}

export async function deleteComment(commentId: string) {
  const response = await httpClient.delete<Comment>(endpoints.comments.detail(commentId));

  return response.data;
}

export async function moderateDeleteComment(commentId: string) {
  const response = await httpClient.delete<Comment>(endpoints.comments.moderationDelete(commentId));

  return response.data;
}
