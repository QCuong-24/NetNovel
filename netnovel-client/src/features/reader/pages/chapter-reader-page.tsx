import { Link, useNavigate, useParams } from 'react-router-dom';
import { useEffect, useMemo, useRef } from 'react';
import { AdSlot } from '@/features/ads/components/ad-slot';
import { Button } from '@/components/ui/button';
import { ChapterNavigation } from '@/features/chapters/components/chapter-navigation';
import { useChapter, useNovelChapters } from '@/features/chapters/hooks/use-chapters';
import { CommentSection } from '@/features/comments/components/comment-section';
import { hasAuthTokens } from '@/features/auth/lib/auth-storage';
import { useUpdateLastReadMutation } from '@/features/collection/hooks/use-collection';
import { useIncreaseNovelViewMutation } from '@/features/novels/hooks/use-novels';
import { cn } from '@/lib/utils';
import { ReaderToolbar } from '../components/reader-toolbar';
import { useReaderSettings } from '../hooks/use-reader-settings';

export function ChapterReaderPage() {
  const navigate = useNavigate();
  const { chapterId, novelId } = useParams();
  const { settings, classes, updateSetting } = useReaderSettings();
  const { data: chapter, isError, isLoading } = useChapter(chapterId);
  const chapterNovelId = chapter?.novelId ? String(chapter.novelId) : novelId;
  const viewedChapterRef = useRef<string | null>(null);
  const increaseViewMutation = useIncreaseNovelViewMutation(chapterNovelId);
  const updateLastReadMutation = useUpdateLastReadMutation();
  const { data: chapters = [] } = useNovelChapters(chapterNovelId);
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
    increaseViewMutation.mutate();
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

  return (
    <div className={cn('min-h-screen text-reader-page-foreground', classes.background)}>
      <ReaderToolbar
        backTo={backToNovel}
        chapterId={chapter?.chapterId ? String(chapter.chapterId) : chapterId}
        editTo={editTo}
        novelId={chapterNovelId}
        settings={settings}
        onChange={updateSetting}
      />
      <article className={cn('mx-auto grid gap-8 px-4 py-8 md:py-12', classes.container)}>
        {isLoading ? (
          <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">
            Loading chapter...
          </div>
        ) : null}

        {isError || !chapter ? (
          <div className="grid min-h-64 place-items-center gap-4 text-center">
            <p className="font-semibold">Chapter not found.</p>
            <Button asChild className="mx-auto w-fit" variant="outline">
              <Link to={backToNovel}>Back to novel</Link>
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
            <div className={cn('grid gap-6 whitespace-pre-wrap', classes.content)}>
              {paragraphs?.length ? (
                paragraphs.map((paragraph, index) => <p key={`${index}-${paragraph.slice(0, 24)}`}>{paragraph}</p>)
              ) : (
                <p>{chapter.content}</p>
              )}
            </div>
            <ChapterNavigation chapters={chapters} currentChapterId={chapter.chapterId} />
            <AdSlot slot="reader_after_chapter" />
            <CommentSection target={{ id: String(chapter.chapterId), type: 'chapter' }} />
          </>
        ) : null}
      </article>
    </div>
  );
}
