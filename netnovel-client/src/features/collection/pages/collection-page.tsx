import { BookOpen, Bookmark, HeartHandshake, Sparkles } from 'lucide-react';
import type { ReactNode } from 'react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { NovelCard } from '@/features/novels/components/novel-card';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { useHashTab } from '@/hooks/use-hash-tab';
import { cn } from '@/lib/utils';
import { useForYouRecommendations } from '@/features/recommendations/hooks/use-recommendations';
import { useBookmarks, useFollowedNovels, useLastReading } from '../hooks/use-collection';
import type { Bookmark as BookmarkItem, BookmarkKind, LastReadNovel } from '../types';

const bookmarkKinds: BookmarkKind[] = ['novels', 'chapters'];
type CollectionTab = 'lastReading' | 'bookmarks' | 'followedNovels' | 'recommendations';

const collectionTabs: Array<{ icon: typeof BookOpen; key: CollectionTab; labelKey: string }> = [
  { icon: BookOpen, key: 'lastReading', labelKey: 'collection.lastReading.title' },
  { icon: Bookmark, key: 'bookmarks', labelKey: 'collection.bookmarks.title' },
  { icon: HeartHandshake, key: 'followedNovels', labelKey: 'collection.followedNovels.title' },
  { icon: Sparkles, key: 'recommendations', labelKey: 'collection.recommendations.title' },
];
const collectionTabKeys = collectionTabs.map((tab) => tab.key);

export function CollectionPage() {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useHashTab(collectionTabKeys, 'lastReading');
  const [bookmarkKind, setBookmarkKind] = useState<BookmarkKind>('novels');
  const lastReadingQuery = useLastReading();
  const bookmarksQuery = useBookmarks(bookmarkKind);
  const followedNovelsQuery = useFollowedNovels();
  const recommendationsQuery = useForYouRecommendations();
  const lastReading = lastReadingQuery.data?.content ?? [];
  const bookmarks = bookmarksQuery.data?.content ?? [];
  const followedNovels = followedNovelsQuery.data?.content ?? [];
  const recommendations = recommendationsQuery.data ?? [];

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <header className="grid gap-2">
        <p className="text-sm font-semibold uppercase text-primary">{t('collection.eyebrow')}</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">{t('collection.title')}</h1>
        <p className="max-w-3xl text-sm leading-6 text-muted-foreground">{t('collection.description')}</p>
      </header>

      <div className="flex w-full flex-wrap gap-2 rounded-lg border bg-card p-1">
        {collectionTabs.map((tab) => {
          const Icon = tab.icon;

          return (
            <Button
              className="flex-1 sm:flex-none"
              key={tab.key}
              type="button"
              variant={activeTab === tab.key ? 'default' : 'ghost'}
              onClick={() => setActiveTab(tab.key)}
            >
              <Icon />
              {t(tab.labelKey)}
            </Button>
          );
        })}
      </div>

      {activeTab === 'lastReading' ? (
        <CollectionSection
          hasContent={lastReading.length > 0}
          icon={<BookOpen className="size-5 text-primary" />}
          isLoading={lastReadingQuery.isLoading}
          title={t('collection.lastReading.title')}
          emptyText={t('collection.lastReading.empty')}
        >
          <div className="grid gap-3 md:grid-cols-2">
            {lastReading.map((item) => (
              <LastReadingRow item={item} key={item.lastReadId} />
            ))}
          </div>
        </CollectionSection>
      ) : null}

      {activeTab === 'bookmarks' ? (
        <CollectionSection
          action={
            <div className="flex rounded-md border bg-muted p-1">
              {bookmarkKinds.map((kind) => (
                <button
                  className={cn(
                    'rounded px-3 py-1.5 text-sm font-semibold text-muted-foreground transition-colors hover:text-foreground',
                    bookmarkKind === kind && 'bg-background text-foreground shadow-sm',
                  )}
                  key={kind}
                  type="button"
                  onClick={() => setBookmarkKind(kind)}
                >
                  {t(`collection.bookmarks.filters.${kind}`)}
                </button>
              ))}
            </div>
          }
          hasContent={bookmarks.length > 0}
          icon={<Bookmark className="size-5 text-primary" />}
          isLoading={bookmarksQuery.isLoading}
          title={t('collection.bookmarks.title')}
          emptyText={t('collection.bookmarks.empty')}
        >
          <div className="grid gap-3 md:grid-cols-2">
            {bookmarks.map((bookmark) => (
              <BookmarkRow bookmark={bookmark} key={bookmark.bookmarkId} />
            ))}
          </div>
        </CollectionSection>
      ) : null}

      {activeTab === 'followedNovels' ? (
        <CollectionSection
          hasContent={followedNovels.length > 0}
          icon={<HeartHandshake className="size-5 text-primary" />}
          isLoading={followedNovelsQuery.isLoading}
          title={t('collection.followedNovels.title')}
          emptyText={t('collection.followedNovels.empty')}
        >
          <div className="grid grid-cols-2 items-stretch gap-3 sm:gap-4 lg:grid-cols-3 xl:grid-cols-6">
            {followedNovels.map((item) => (
              <div className="grid h-full grid-rows-[1fr_auto] gap-2" key={item.followId}>
                <NovelCard novel={item.novel} />
                <p className="text-xs font-semibold text-muted-foreground">
                  {t('collection.followedNovels.followedAt', { date: formatDateTime(item.followedAt ?? undefined) })}
                </p>
              </div>
            ))}
          </div>
        </CollectionSection>
      ) : null}

      {activeTab === 'recommendations' ? (
        <CollectionSection
          hasContent={recommendations.length > 0}
          icon={<Sparkles className="size-5 text-primary" />}
          isLoading={recommendationsQuery.isLoading}
          title={t('collection.recommendations.title')}
          emptyText={t('collection.recommendations.empty')}
        >
          <div className="grid grid-cols-2 items-stretch gap-3 sm:gap-4 lg:grid-cols-3 xl:grid-cols-6">
            {recommendations.map((item) => (
              <div className="grid h-full grid-rows-[1fr_auto] gap-2" key={item.novel.novelId}>
                <NovelCard novel={item.novel} />
                <p className="text-xs font-semibold text-muted-foreground">{t(`collection.recommendations.reasons.${item.reason}`)}</p>
              </div>
            ))}
          </div>
        </CollectionSection>
      ) : null}
    </main>
  );
}

function CollectionSection({
  action,
  children,
  emptyText,
  hasContent,
  icon,
  isLoading,
  title,
}: {
  action?: ReactNode;
  children: ReactNode;
  emptyText: string;
  hasContent: boolean;
  icon: ReactNode;
  isLoading: boolean;
  title: string;
}) {
  const { t } = useTranslation();

  return (
    <Card className="panel-motion">
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            {icon}
            {title}
          </CardTitle>
          {action}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <p className="text-sm font-semibold text-muted-foreground">{t('collection.loading')}</p>
        ) : hasContent ? (
          children
        ) : (
          <p className="rounded-lg border border-dashed p-4 text-sm text-muted-foreground">{emptyText}</p>
        )}
      </CardContent>
    </Card>
  );
}

function BookmarkRow({ bookmark }: { bookmark: BookmarkItem }) {
  const { t } = useTranslation();
  const novelHref = bookmark.novelId ? `/novels/${bookmark.novelId}` : '#';
  const chapterHref =
    bookmark.novelId && bookmark.chapterId ? `/novels/${bookmark.novelId}/chapters/${bookmark.chapterId}` : novelHref;
  const primaryHref = bookmark.chapterId ? chapterHref : novelHref;

  return (
    <div className="grid gap-3 rounded-lg border bg-card p-3 sm:grid-cols-[64px_minmax(0,1fr)_auto]">
      <Link className="block overflow-hidden rounded-md bg-muted" to={novelHref}>
        {bookmark.coverImageUrl ? (
          <img alt={bookmark.novelTitle ?? ''} className="aspect-[3/4] w-full object-cover" src={bookmark.coverImageUrl} />
        ) : (
          <div className="grid aspect-[3/4] place-items-center bg-cover-gradient text-primary-foreground">
            <BookOpen className="size-6" />
          </div>
        )}
      </Link>
      <div className="grid content-center gap-1">
        <Link className="line-clamp-1 text-sm font-bold hover:text-primary hover:underline" to={primaryHref}>
          {bookmark.chapterId
            ? t('collection.bookmarks.chapterTitle', {
                number: bookmark.chapterNumber ?? '',
                title: bookmark.chapterTitle ?? t('chapters.chapter'),
              })
            : bookmark.novelTitle}
        </Link>
        <Link className="line-clamp-1 text-sm text-muted-foreground hover:text-foreground" to={novelHref}>
          {bookmark.novelTitle}
        </Link>
        <p className="text-xs text-muted-foreground">{formatDateTime(bookmark.createdAt ?? undefined)}</p>
      </div>
      <Button className="self-center" size="sm" variant="outline" asChild>
        <Link to={primaryHref}>{t('collection.open')}</Link>
      </Button>
    </div>
  );
}

function LastReadingRow({ item }: { item: LastReadNovel }) {
  const { t } = useTranslation();
  const novelHref = item.novelId ? `/novels/${item.novelId}` : '#';
  const chapterHref =
    item.novelId && item.chapterId ? `/novels/${item.novelId}/chapters/${item.chapterId}` : novelHref;

  return (
    <div className="grid gap-3 rounded-lg border bg-card p-3 sm:grid-cols-[64px_minmax(0,1fr)_auto]">
      <Link className="block overflow-hidden rounded-md bg-muted" to={novelHref}>
        {item.coverImageUrl ? (
          <img alt={item.novelTitle ?? ''} className="aspect-[3/4] w-full object-cover" src={item.coverImageUrl} />
        ) : (
          <div className="grid aspect-[3/4] place-items-center bg-cover-gradient text-primary-foreground">
            <BookOpen className="size-6" />
          </div>
        )}
      </Link>
      <div className="grid content-center gap-1">
        <Link className="line-clamp-1 text-sm font-bold hover:text-primary hover:underline" to={chapterHref}>
          {item.chapterId
            ? t('collection.bookmarks.chapterTitle', {
                number: item.chapterNumber ?? '',
                title: item.chapterTitle ?? t('chapters.chapter'),
              })
            : item.novelTitle}
        </Link>
        <Link className="line-clamp-1 text-sm text-muted-foreground hover:text-foreground" to={novelHref}>
          {item.novelTitle}
        </Link>
        <p className="text-xs text-muted-foreground">
          {t('collection.lastReading.lastReadAt', { date: formatDateTime(item.lastReadAt ?? undefined) })}
        </p>
      </div>
      <Button className="self-center" size="sm" variant="outline" asChild>
        <Link to={chapterHref}>{t('collection.open')}</Link>
      </Button>
    </div>
  );
}
