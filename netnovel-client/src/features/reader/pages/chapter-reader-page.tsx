import { Bookmark, Volume2, X } from 'lucide-react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AdSlot } from '@/features/ads/components/ad-slot';
import { Button } from '@/components/ui/button';
import { ChapterAudioPlayer } from '@/features/audio/components/chapter-audio-player';
import { ChapterNavigation } from '@/features/chapters/components/chapter-navigation';
import { useChapter, useNovelChapters } from '@/features/chapters/hooks/use-chapters';
import { CommentSection } from '@/features/comments/components/comment-section';
import { hasAuthTokens } from '@/features/auth/lib/auth-storage';
import {
  useChapterBookmarkStatus,
  useToggleChapterBookmarkMutation,
  useUpdateLastReadMutation,
} from '@/features/collection/hooks/use-collection';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { useIncreaseNovelViewMutation } from '@/features/novels/hooks/use-novels';
import { getApiErrorMessage } from '@/lib/api/api-error';
import { cn } from '@/lib/utils';
import { ReaderToolbar } from '../components/reader-toolbar';
import { useReaderSettings } from '../hooks/use-reader-settings';

export function ChapterReaderPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { chapterId, novelId } = useParams();
  const [isAudioOpen, setIsAudioOpen] = useState(false);
  const [isAudioPlaying, setIsAudioPlaying] = useState(false);
  const { settings, classes, updateSetting } = useReaderSettings();
  const { data: user } = useCurrentUser();
  const { data: chapter, error, isError, isLoading } = useChapter(chapterId);
  const chapterNovelId = chapter?.novelId ? String(chapter.novelId) : novelId;
  const viewedChapterRef = useRef<string | null>(null);
  const increaseViewMutation = useIncreaseNovelViewMutation(chapterNovelId);
  const updateLastReadMutation = useUpdateLastReadMutation();
  const { data: isChapterBookmarked = false } = useChapterBookmarkStatus(chapterId);
  const bookmarkMutation = useToggleChapterBookmarkMutation(chapterId ?? '', isChapterBookmarked);
  const { data: chapters = [], isLoading: isChaptersLoading } = useNovelChapters(chapterNovelId);
  const backToNovel = chapter?.novelId ? `/novels/${chapter.novelId}` : `/novels/${novelId ?? ''}`;
  const editTo =
    chapter?.novelId && chapter?.chapterId
      ? `/novels/${chapter.novelId}/chapters/${chapter.chapterId}/edit`
      : undefined;
  const paragraphs = chapter?.content
    .split(/\n{2,}/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean);
  const sortedChapters = useMemo(
    () => [...chapters].sort((left, right) => left.chapterNumber - right.chapterNumber),
    [chapters],
  );
  const currentChapterIndex = chapter
    ? sortedChapters.findIndex((chapterSummary) => chapterSummary.chapterId === chapter.chapterId)
    : -1;
  const previousChapter = currentChapterIndex > 0 ? sortedChapters[currentChapterIndex - 1] : undefined;
  const nextChapter =
    currentChapterIndex >= 0 && currentChapterIndex < sortedChapters.length - 1
      ? sortedChapters[currentChapterIndex + 1]
      : undefined;

  useEffect(() => {
    if (!chapter?.chapterId || !chapterNovelId) {
      return;
    }

    const viewKey = `${chapterNovelId}:${chapter.chapterId}`;
    if (viewedChapterRef.current === viewKey) {
      return;
    }

    viewedChapterRef.current = viewKey;
    increaseViewMutation.mutate(String(chapter.chapterId));
    if (hasAuthTokens()) {
      updateLastReadMutation.mutate({
        chapterId: String(chapter.chapterId),
        novelId: chapterNovelId,
      });
    }
  }, [chapter?.chapterId, chapterNovelId, increaseViewMutation, updateLastReadMutation]);

  useEffect(() => {
    function isEditableTarget(target: EventTarget | null) {
      if (!(target instanceof HTMLElement)) {
        return false;
      }

      return Boolean(target.closest('input, textarea, select, [contenteditable="true"]'));
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.altKey || event.ctrlKey || event.metaKey || event.shiftKey || isEditableTarget(event.target)) {
        return;
      }

      if (event.key === 'ArrowLeft' && previousChapter) {
        event.preventDefault();
        navigate(`/novels/${previousChapter.novelId}/chapters/${previousChapter.chapterId}`);
      }

      if (event.key === 'ArrowRight' && nextChapter) {
        event.preventDefault();
        navigate(`/novels/${nextChapter.novelId}/chapters/${nextChapter.chapterId}`);
      }
    }

    window.addEventListener('keydown', handleKeyDown);

    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [navigate, nextChapter, previousChapter]);

  useEffect(() => {
    if (!user) {
      setIsAudioOpen(false);
      setIsAudioPlaying(false);
    }
  }, [user]);

  return (
    <div className={cn('min-h-screen min-w-0 max-w-full overflow-x-hidden pt-14 text-reader-page-foreground', classes.background)}>
      <ReaderToolbar
        backTo={backToNovel}
        chapterId={chapter?.chapterId ? String(chapter.chapterId) : chapterId}
        editTo={editTo}
        novelId={chapterNovelId}
        settings={settings}
        onChange={updateSetting}
      />
      {user && chapterId ? (
        <>
          <Button
            aria-expanded={isAudioOpen}
            aria-label={t('audio.title')}
            className={cn(
              'fixed right-16 top-[4.5rem] z-30 overflow-visible bg-background/70 shadow-md backdrop-blur hover:bg-background/90',
              isAudioPlaying && 'text-primary',
            )}
            size="icon"
            title={t('audio.title')}
            type="button"
            variant="outline"
            onClick={() => setIsAudioOpen((current) => !current)}
          >
            {isAudioPlaying ? (
              <>
                <span className="absolute inset-1 rounded-full border border-primary/50 animate-ping" />
                <span className="absolute inset-2 rounded-full border border-primary/30 animate-pulse" />
              </>
            ) : null}
            <Volume2 className="relative z-10" />
          </Button>
          <div
            className={cn(
              'panel-motion fixed right-4 top-[7.5rem] z-40 w-[min(92vw,28rem)] rounded-xl border bg-background/95 p-3 shadow-2xl backdrop-blur',
              !isAudioOpen && 'pointer-events-none invisible opacity-0',
            )}
          >
              <div className="mb-2 flex items-center justify-between gap-3">
                <p className="text-sm font-extrabold">{t('audio.title')}</p>
                <Button
                  aria-label={t('audio.close')}
                  size="icon"
                  type="button"
                  variant="ghost"
                  onClick={() => setIsAudioOpen(false)}
                >
                  <X />
                </Button>
              </div>
              <ChapterAudioPlayer key={chapterId} chapterId={chapterId} onPlayingChange={setIsAudioPlaying} />
            </div>
        </>
      ) : null}
      {user && chapterId ? (
        <Button
          aria-label={isChapterBookmarked ? t('bookmarkActions.bookmarked') : t('bookmarkActions.bookmark')}
          className={cn(
            'fixed right-4 top-[4.5rem] z-30 bg-background/70 shadow-md backdrop-blur transition-opacity hover:bg-background/90',
            !isChapterBookmarked && 'opacity-40 hover:opacity-100',
          )}
          disabled={bookmarkMutation.isPending}
          size="icon"
          title={isChapterBookmarked ? t('bookmarkActions.bookmarked') : t('bookmarkActions.bookmark')}
          type="button"
          variant="outline"
          onClick={() => bookmarkMutation.mutate()}
        >
          <Bookmark fill={isChapterBookmarked ? 'currentColor' : 'none'} />
        </Button>
      ) : null}
      <article className={cn('mx-auto grid w-full min-w-0 max-w-full gap-8 px-4 py-8 md:py-12', classes.container)}>
        {isLoading ? (
          <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">
            Loading chapter...
          </div>
        ) : null}

        {isError || !chapter ? (
          <div className="grid min-h-64 place-items-center gap-4 text-center">
            <p className="font-semibold">{getApiErrorMessage(error, t('chapterPages.notFound'))}</p>
            <Button asChild className="mx-auto w-fit" variant="outline">
              <Link to={backToNovel}>{t('chapterPages.backToNovel')}</Link>
            </Button>
          </div>
        ) : null}

        {chapter ? (
          <>
            <AdSlot slot="reader_top" />
            <header className="grid gap-3 text-center">
              <Link
                className="text-sm font-semibold uppercase tracking-wide text-muted-foreground transition-colors hover:text-primary hover:underline"
                to={`/novels/${chapter.novelId}`}
              >
                {chapter.novelTitle}
              </Link>
              <h1 className="text-3xl font-bold tracking-normal md:text-4xl">
                Chapter {chapter.chapterNumber}: {chapter.title}
              </h1>
            </header>
            <ChapterNavigation chapters={chapters} currentChapterId={chapter.chapterId} />
            <div className={cn('grid w-full min-w-0 max-w-full gap-6 whitespace-pre-wrap break-all [overflow-wrap:anywhere]', classes.content)}>
              {paragraphs?.length ? (
                paragraphs.map((paragraph, index) => (
                  <p className="min-w-0 max-w-full break-all [overflow-wrap:anywhere]" key={`${index}-${paragraph.slice(0, 24)}`}>
                    {paragraph}
                  </p>
                ))
              ) : (
                <p className="min-w-0 max-w-full break-all [overflow-wrap:anywhere]">{chapter.content}</p>
              )}
            </div>
            <ChapterNavigation chapters={chapters} currentChapterId={chapter.chapterId} />
            <AdSlot slot="reader_after_chapter" />
            <CommentSection isAnchorReady={!isChaptersLoading} target={{ id: String(chapter.chapterId), type: 'chapter' }} />
          </>
        ) : null}
      </article>
    </div>
  );
}
