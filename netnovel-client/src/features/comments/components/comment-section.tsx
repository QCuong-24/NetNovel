import { type FormEvent, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useLocation } from 'react-router-dom';
import { MessageCircle, Pencil, RefreshCw, Reply, Trash2, X } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { routes } from '@/config/routes';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import type { User } from '@/features/auth/types';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { cn } from '@/lib/utils';
import {
  useCommentReplies,
  useCommentContext,
  useComments,
  useCreateCommentMutation,
  useCreateCommentReplyMutation,
  useDeleteCommentMutation,
  useUpdateCommentMutation,
} from '../hooks/use-comments';
import type { Comment, CommentTarget } from '../types';

type CommentSectionProps = {
  isAnchorReady?: boolean;
  target: CommentTarget;
};

function canModerateComments(user?: User) {
  return Boolean(user?.roles?.some((role) => role === 'MANAGER' || role === 'ADMIN'));
}

function getCommentIdFromHash(hash: string) {
  return hash.match(/^#comment-(\d+)$/)?.[1];
}

export function CommentSection({ isAnchorReady = true, target }: CommentSectionProps) {
  const { t } = useTranslation();
  const location = useLocation();
  const [page, setPage] = useState(0);
  const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');
  const [selectedCommentId, setSelectedCommentId] = useState<string | undefined>(() => getCommentIdFromHash(location.hash));
  const commentsQuery = useComments(target, { page, size: 10, sort: `createdAt,${sortDirection}` });
  const commentContextQuery = useCommentContext(selectedCommentId);
  const createMutation = useCreateCommentMutation(target);
  const commentsPage = commentsQuery.data;
  const comments = [...(commentsPage?.content ?? [])].sort((left, right) => {
    const leftTime = new Date(left.createdAt ?? 0).getTime();
    const rightTime = new Date(right.createdAt ?? 0).getTime();

    return sortDirection === 'desc' ? rightTime - leftTime : leftTime - rightTime;
  });
  const commentContext = [...(commentContextQuery.data ?? [])].reverse();
  const contextRoot = commentContext[0];
  const contextBelongsToTarget = contextRoot
    ? target.type === 'novel'
      ? String(contextRoot.novelId) === target.id
      : String(contextRoot.chapterId) === target.id
    : false;
  const visibleComments =
    contextRoot && contextBelongsToTarget && !comments.some((comment) => comment.commentId === contextRoot.commentId)
      ? [contextRoot, ...comments]
      : comments;
  const contextReplyParentIds = new Set(commentContext.slice(0, -1).map((comment) => comment.commentId));

  useEffect(() => {
    setSelectedCommentId(getCommentIdFromHash(location.hash));
  }, [location.hash]);

  useEffect(() => {
    if (!selectedCommentId || commentContextQuery.isLoading || !isAnchorReady) {
      return;
    }

    let scrollTimeoutId: number | undefined;
    const intervalId = window.setInterval(() => {
      const commentElement = document.getElementById(`comment-${selectedCommentId}`);

      if (commentElement) {
        window.clearInterval(intervalId);
        scrollTimeoutId = window.setTimeout(() => {
          commentElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }, 250);
      }
    }, 100);
    const timeoutId = window.setTimeout(() => window.clearInterval(intervalId), 3000);

    return () => {
      window.clearInterval(intervalId);
      window.clearTimeout(timeoutId);
      if (scrollTimeoutId) {
        window.clearTimeout(scrollTimeoutId);
      }
    };
  }, [commentContextQuery.isLoading, isAnchorReady, selectedCommentId]);

  function selectComment(commentId: number) {
    const hash = `#comment-${commentId}`;
    window.history.replaceState(null, '', hash);
    setSelectedCommentId(String(commentId));
  }

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between gap-3">
        <CardTitle className="flex items-center gap-2">
          <MessageCircle className="size-5 text-primary" />
          {t('comments.title')}
        </CardTitle>
        <div className="flex flex-wrap items-center justify-end gap-2">
          <select
            className="h-9 rounded-md border bg-background px-3 text-sm font-semibold outline-none transition focus-visible:ring-2 focus-visible:ring-ring"
            value={sortDirection}
            onChange={(event) => {
              setPage(0);
              setSortDirection(event.target.value as 'desc' | 'asc');
            }}
          >
            <option value="desc">{t('comments.newest')}</option>
            <option value="asc">{t('comments.oldest')}</option>
          </select>
          <Badge variant="outline">{t('comments.total', { count: commentsPage?.totalElements ?? 0 })}</Badge>
        </div>
      </CardHeader>
      <CardContent className="grid gap-5">
        <CommentForm
          isSubmitting={createMutation.isPending}
          placeholder={t('comments.placeholder')}
          submitLabel={t('comments.post')}
          onSubmit={(content) => createMutation.mutate({ content })}
        />

        {commentsQuery.isLoading ? (
          <div className="grid min-h-32 place-items-center text-sm font-semibold text-muted-foreground">
            {t('comments.loading')}
          </div>
        ) : null}

        {!commentsQuery.isLoading && !visibleComments.length ? (
          <div className="rounded-lg border border-dashed p-5 text-sm font-semibold text-muted-foreground">
            {t('comments.empty')}
          </div>
        ) : null}

        {visibleComments.length ? (
          <div className="grid gap-4">
            {visibleComments.map((comment) => (
              <CommentItem
                activeCommentId={selectedCommentId}
                comment={comment}
                contextReplyParentIds={contextReplyParentIds}
                key={comment.commentId}
                onSelect={selectComment}
              />
            ))}
          </div>
        ) : null}

        {commentsPage && commentsPage.totalPages > 1 ? (
          <div className="flex flex-col gap-2 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
            <span className="text-sm font-bold text-muted-foreground">
              {t('comments.pageInfo', { page: commentsPage.number + 1, total: commentsPage.totalPages })}
            </span>
            <div className="flex gap-2">
              <Button
                disabled={commentsPage.first || commentsQuery.isLoading}
                type="button"
                variant="outline"
                onClick={() => setPage((current) => Math.max(0, current - 1))}
              >
                {t('rankingPage.previous')}
              </Button>
              <Button
                disabled={commentsPage.last || commentsQuery.isLoading}
                type="button"
                variant="outline"
                onClick={() => setPage((current) => current + 1)}
              >
                {t('rankingPage.next')}
              </Button>
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function CommentItem({
  activeCommentId,
  comment,
  contextReplyParentIds,
  onSelect,
}: {
  activeCommentId?: string;
  comment: Comment;
  contextReplyParentIds: Set<number>;
  onSelect: (commentId: number) => void;
}) {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const [showReplies, setShowReplies] = useState(false);
  const [isReplying, setIsReplying] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const isOwner = user?.userId === comment.userId;
  const canEdit = isOwner && !comment.deleted;
  const canDelete = !comment.deleted && (isOwner || canModerateComments(user));
  const repliesQuery = useCommentReplies(String(comment.commentId), showReplies);
  const replyMutation = useCreateCommentReplyMutation(String(comment.commentId));
  const updateMutation = useUpdateCommentMutation();
  const deleteMutation = useDeleteCommentMutation();

  useEffect(() => {
    if (contextReplyParentIds.has(comment.commentId)) {
      setShowReplies(true);
    }
  }, [comment.commentId, contextReplyParentIds]);

  return (
    <article
      className={cn(
        'grid gap-3 rounded-lg border bg-background p-4 transition-colors',
        comment.deleted && 'opacity-75',
        String(comment.commentId) === activeCommentId && 'border-primary bg-primary/10 dark:bg-primary/20',
      )}
      id={`comment-${comment.commentId}`}
      onClick={() => onSelect(comment.commentId)}
    >
      <div className="flex gap-3">
        <Avatar name={comment.username} src={comment.userAvatarUrl} userId={comment.userId} />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="font-extrabold">{comment.username}</h3>
            <span className="text-xs font-semibold text-muted-foreground">{formatDateTime(comment.createdAt ?? undefined)}</span>
            {comment.deleted ? <Badge variant="outline">{t('comments.deleted')}</Badge> : null}
          </div>

          {isEditing ? (
            <CommentForm
              initialValue={comment.content}
              isSubmitting={updateMutation.isPending}
              placeholder={t('comments.placeholder')}
              submitLabel={t('comments.save')}
              onCancel={() => setIsEditing(false)}
              onSubmit={(content) =>
                updateMutation.mutate(
                  { commentId: String(comment.commentId), payload: { content } },
                  { onSuccess: () => setIsEditing(false) },
                )
              }
            />
          ) : (
            <p className="mt-2 whitespace-pre-line text-sm leading-6 text-muted-foreground">{comment.content}</p>
          )}
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-2 pl-0 sm:pl-[52px]">
        <Button
          size="sm"
          type="button"
          variant="ghost"
          onClick={() => {
            setShowReplies((current) => !current);
            setIsReplying(false);
          }}
        >
          <MessageCircle />
          {t('comments.replies', { count: comment.replyCount ?? 0 })}
        </Button>

        {!comment.deleted ? (
          <Button
            size="sm"
            type="button"
            variant="ghost"
            onClick={() => {
              setShowReplies(true);
              setIsReplying((current) => !current);
            }}
          >
            <Reply />
            {t('comments.reply')}
          </Button>
        ) : null}

        {canEdit ? (
          <Button size="sm" type="button" variant="ghost" onClick={() => setIsEditing((current) => !current)}>
            <Pencil />
            {t('comments.edit')}
          </Button>
        ) : null}

        {canDelete ? (
          <Button
            disabled={deleteMutation.isPending}
            size="sm"
            type="button"
            variant="ghost"
            onClick={() =>
              deleteMutation.mutate({
                commentId: String(comment.commentId),
                moderate: !isOwner,
              })
            }
          >
            <Trash2 />
            {t('comments.delete')}
          </Button>
        ) : null}
      </div>

      {showReplies ? (
        <div className="grid gap-3 border-l pl-4 sm:ml-[52px]">
          {isReplying ? (
            <CommentForm
              isSubmitting={replyMutation.isPending}
              placeholder={t('comments.replyPlaceholder')}
              submitLabel={t('comments.reply')}
              onCancel={() => setIsReplying(false)}
              onSubmit={(content) =>
                replyMutation.mutate(
                  { content },
                  { onSuccess: () => setIsReplying(false) },
                )
              }
            />
          ) : null}

          {repliesQuery.isLoading ? (
            <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
              <RefreshCw className="size-4 animate-spin" />
              {t('comments.loadingReplies')}
            </div>
          ) : null}

          {repliesQuery.data?.map((reply) => (
            <CommentItem
              activeCommentId={activeCommentId}
              comment={reply}
              contextReplyParentIds={contextReplyParentIds}
              key={reply.commentId}
              onSelect={onSelect}
            />
          ))}
        </div>
      ) : null}
    </article>
  );
}

function CommentForm({
  initialValue = '',
  isSubmitting,
  onCancel,
  onSubmit,
  placeholder,
  submitLabel,
}: {
  initialValue?: string;
  isSubmitting: boolean;
  onCancel?: () => void;
  onSubmit: (content: string) => void;
  placeholder: string;
  submitLabel: string;
}) {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const [content, setContent] = useState(initialValue);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalizedContent = content.trim();

    if (!normalizedContent) {
      return;
    }

    onSubmit(normalizedContent);
    if (!initialValue) {
      setContent('');
    }
  }

  if (!user) {
    return (
      <div className="rounded-lg border border-dashed p-4 text-sm font-semibold text-muted-foreground">
        <Link className="font-extrabold text-primary hover:underline" to={routes.login}>
          {t('auth.login')}
        </Link>{' '}
        {t('comments.loginPrompt')}
      </div>
    );
  }

  return (
    <form className="grid gap-3" onSubmit={submit}>
      <textarea
        className="min-h-24 resize-y rounded-md border bg-background px-3 py-2 text-sm leading-6 outline-none transition focus-visible:ring-2 focus-visible:ring-ring"
        placeholder={placeholder}
        value={content}
        onChange={(event) => setContent(event.target.value)}
      />
      <div className="flex flex-wrap justify-end gap-2">
        {onCancel ? (
          <Button type="button" variant="ghost" onClick={onCancel}>
            <X />
            {t('novelForm.cancel')}
          </Button>
        ) : null}
        <Button disabled={isSubmitting || !content.trim()} type="submit">
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}

function Avatar({ name, src, userId }: { name: string; src?: string | null; userId: number }) {
  return (
    <Link
      aria-label={`View ${name}'s profile`}
      className="grid size-10 shrink-0 place-items-center overflow-hidden rounded-full bg-primary text-xs font-extrabold text-primary-foreground transition-opacity hover:opacity-80"
      to={routes.userProfile(userId)}
    >
      {src ? <img alt={name} className="size-full object-cover" src={src} /> : name.slice(0, 2).toUpperCase()}
    </Link>
  );
}
