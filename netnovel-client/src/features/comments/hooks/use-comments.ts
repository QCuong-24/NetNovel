import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { queryKeys } from '@/config/query-keys';
import { getApiErrorMessage } from '@/lib/api/api-error';
import {
  createComment,
  createCommentReply,
  deleteComment,
  getCommentReplies,
  getCommentContext,
  getComments,
  moderateDeleteComment,
  updateComment,
} from '../api/comment-api';
import type { CommentPayload, CommentTarget } from '../types';

type CommentListParams = {
  page?: number;
  size?: number;
  sort?: string;
};

export function useComments(target: CommentTarget, params: CommentListParams) {
  return useQuery({
    queryKey: [...queryKeys.comments, 'list', target, params],
    queryFn: () => getComments(target, params),
    enabled: Boolean(target.id),
  });
}

export function useCommentReplies(commentId: string, enabled: boolean) {
  return useQuery({
    queryKey: [...queryKeys.comments, 'replies', commentId],
    queryFn: () => getCommentReplies(commentId),
    enabled,
  });
}

export function useCommentContext(commentId?: string) {
  return useQuery({
    queryKey: [...queryKeys.comments, 'context', commentId],
    queryFn: () => getCommentContext(commentId ?? ''),
    enabled: Boolean(commentId),
  });
}

export function useCreateCommentMutation(target: CommentTarget) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CommentPayload) => createComment(target, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comments });
      toast.success('Comment posted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not post comment'));
    },
  });
}

export function useCreateCommentReplyMutation(commentId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CommentPayload) => createCommentReply(commentId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comments });
      toast.success('Reply posted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not post reply'));
    },
  });
}

export function useUpdateCommentMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ commentId, payload }: { commentId: string; payload: CommentPayload }) => updateComment(commentId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comments });
      toast.success('Comment updated');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not update comment'));
    },
  });
}

export function useDeleteCommentMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ commentId, moderate }: { commentId: string; moderate?: boolean }) =>
      moderate ? moderateDeleteComment(commentId) : deleteComment(commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.comments });
      toast.success('Comment deleted');
    },
    onError: (error) => {
      toast.error(getApiErrorMessage(error, 'Could not delete comment'));
    },
  });
}
