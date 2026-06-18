import { ChevronFirst, ChevronLast, ChevronLeft, ChevronRight, RefreshCw, Search } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router-dom';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import type { User } from '@/features/auth/types';
import { NovelCard } from '@/features/novels/components/novel-card';
import { useGenres, useTags } from '@/features/novels/hooks/use-novels';
import type { PageResponse, Novel } from '@/features/novels/types';
import { useAdvancedNovelSearch, usePublicNovelSearch, useReindexNovelsMutation } from '../hooks/use-search';
import type { AdvancedNovelSearchParams, PublicNovelSearchParams, SearchSort } from '../types';

function canUseAdvancedSearch(user?: User) {
  return Boolean(user?.roles?.some((role) => role === 'MANAGER' || role === 'ADMIN'));
}

function isAdmin(user?: User) {
  return Boolean(user?.roles?.includes('ADMIN'));
}

function clampPage(page: number, totalPages: number) {
  if (totalPages <= 0) {
    return 0;
  }

  return Math.min(Math.max(page, 0), totalPages - 1);
}

export function SearchPage() {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const { data: user } = useCurrentUser();
  const showAdvanced = canUseAdvancedSearch(user);
  const showReindex = isAdmin(user);
  const publicQuery = searchParams.get('q') ?? '';
  const [activeTab, setActiveTab] = useState<'public' | 'advanced' | 'reindex'>('public');

  useEffect(() => {
    if (publicQuery.trim()) {
      setActiveTab('public');
    }
  }, [publicQuery]);

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <header className="grid gap-2">
        <p className="text-sm font-semibold uppercase text-primary">{t('nav.search')}</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">{t('searchPage.title')}</h1>
        <p className="max-w-3xl text-sm leading-6 text-muted-foreground">{t('searchPage.description')}</p>
      </header>

      <div className="flex w-full flex-wrap gap-2 rounded-lg border bg-card p-1">
        <Button
          className="flex-1 sm:flex-none"
          type="button"
          variant={activeTab === 'public' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('public')}
        >
          {t('searchPage.public.title')}
        </Button>
        {showAdvanced ? (
          <Button
            className="flex-1 sm:flex-none"
            type="button"
            variant={activeTab === 'advanced' ? 'default' : 'ghost'}
            onClick={() => setActiveTab('advanced')}
          >
            {t('searchPage.advanced.title')}
          </Button>
        ) : null}
        {showReindex ? (
          <Button
            className="flex-1 sm:flex-none"
            type="button"
            variant={activeTab === 'reindex' ? 'default' : 'ghost'}
            onClick={() => setActiveTab('reindex')}
          >
            {t('searchPage.reindex.title')}
          </Button>
        ) : null}
      </div>

      {activeTab === 'public' ? <PublicSearchPanel initialQuery={publicQuery} /> : null}
      {activeTab === 'advanced' && showAdvanced ? <AdvancedSearchPanel /> : null}
      {activeTab === 'reindex' && showReindex ? <ReindexPanel /> : null}
    </main>
  );
}

function PublicSearchPanel({ initialQuery = '' }: { initialQuery?: string }) {
  const { t } = useTranslation();
  const { data: genres = [] } = useGenres();
  const normalizedInitialQuery = initialQuery.trim();
  const [form, setForm] = useState({ q: normalizedInitialQuery, status: '', genre: '', sort: 'relevance' as SearchSort });
  const [params, setParams] = useState<PublicNovelSearchParams>({
    q: normalizedInitialQuery,
    status: '',
    genre: '',
    sort: 'relevance',
    page: 0,
    size: 20,
  });
  const [hasSubmitted, setHasSubmitted] = useState(Boolean(normalizedInitialQuery));
  const searchQuery = usePublicNovelSearch(params, hasSubmitted);

  useEffect(() => {
    setForm({ q: normalizedInitialQuery, status: '', genre: '', sort: 'relevance' });
    setParams((current) => ({
      q: normalizedInitialQuery,
      status: '',
      genre: '',
      sort: 'relevance',
      page: 0,
      size: current.size,
    }));
    setHasSubmitted(Boolean(normalizedInitialQuery));
  }, [normalizedInitialQuery]);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setHasSubmitted(true);
    setParams({ ...form, page: 0, size: params.size });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('searchPage.public.title')}</CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4">
        <form className="grid gap-3 xl:grid-cols-[minmax(0,1fr)_150px_170px_150px_auto]" onSubmit={submit}>
          <Input
            placeholder={t('searchPage.fields.query')}
            value={form.q}
            onChange={(event) => setForm((current) => ({ ...current, q: event.target.value }))}
          />
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={form.status}
            onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
          >
            <option value="">{t('searchPage.fields.anyStatus')}</option>
            <option value="ONGOING">{t('novelForm.statusOptions.ONGOING')}</option>
            <option value="COMPLETED">{t('novelForm.statusOptions.COMPLETED')}</option>
          </select>
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={form.genre}
            onChange={(event) => setForm((current) => ({ ...current, genre: event.target.value }))}
          >
            <option value="">{t('searchPage.fields.anyGenre')}</option>
            {genres.map((genre) => (
              <option key={genre.genreId} value={genre.name}>
                {genre.name}
              </option>
            ))}
          </select>
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={form.sort}
            onChange={(event) => setForm((current) => ({ ...current, sort: event.target.value as SearchSort }))}
          >
            <option value="relevance">{t('searchPage.sort.relevance')}</option>
            <option value="latest">{t('searchPage.sort.latest')}</option>
            <option value="popular">{t('searchPage.sort.popular')}</option>
          </select>
          <Button type="submit">
            <Search />
            {t('searchPage.search')}
          </Button>
        </form>

        <SearchResults
          emptyText={hasSubmitted ? t('searchPage.empty') : t('searchPage.public.prompt')}
          isLoading={searchQuery.isLoading}
          page={searchQuery.data}
          onPageChange={(page) => setParams((current) => ({ ...current, page }))}
          onPageSizeChange={(size) => setParams((current) => ({ ...current, page: 0, size }))}
        />
      </CardContent>
    </Card>
  );
}

function AdvancedSearchPanel() {
  const { t } = useTranslation();
  const { data: genres = [] } = useGenres();
  const { data: tags = [] } = useTags();
  const [form, setForm] = useState({ q: '', status: '', genre: '', tag: '', source: '', crawled: '' });
  const [params, setParams] = useState<AdvancedNovelSearchParams>({
    q: '',
    status: '',
    genre: '',
    tag: '',
    source: '',
    crawled: '',
    page: 0,
    size: 20,
  });
  const [hasSubmitted, setHasSubmitted] = useState(false);
  const searchQuery = useAdvancedNovelSearch(params, hasSubmitted);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setHasSubmitted(true);
    setParams({ ...form, page: 0, size: params.size });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex flex-wrap items-center gap-2">
          {t('searchPage.advanced.title')}
          <Badge variant="secondary">{t('searchPage.advanced.badge')}</Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4">
        <form className="grid gap-3 xl:grid-cols-[minmax(0,1fr)_140px_150px_150px_140px_130px_auto]" onSubmit={submit}>
          <Input
            placeholder={t('searchPage.fields.query')}
            value={form.q}
            onChange={(event) => setForm((current) => ({ ...current, q: event.target.value }))}
          />
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={form.status}
            onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
          >
            <option value="">{t('searchPage.fields.anyStatus')}</option>
            <option value="ONGOING">{t('novelForm.statusOptions.ONGOING')}</option>
            <option value="COMPLETED">{t('novelForm.statusOptions.COMPLETED')}</option>
          </select>
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={form.genre}
            onChange={(event) => setForm((current) => ({ ...current, genre: event.target.value }))}
          >
            <option value="">{t('searchPage.fields.anyGenre')}</option>
            {genres.map((genre) => (
              <option key={genre.genreId} value={genre.name}>
                {genre.name}
              </option>
            ))}
          </select>
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={form.tag}
            onChange={(event) => setForm((current) => ({ ...current, tag: event.target.value }))}
          >
            <option value="">{t('searchPage.fields.anyTag')}</option>
            {tags.map((tag) => (
              <option key={tag.tagId} value={tag.name}>
                {tag.name}
              </option>
            ))}
          </select>
          <Input
            placeholder={t('searchPage.fields.source')}
            value={form.source}
            onChange={(event) => setForm((current) => ({ ...current, source: event.target.value }))}
          />
          <select
            className="h-10 rounded-md border bg-background px-3 text-sm font-semibold text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={form.crawled}
            onChange={(event) => setForm((current) => ({ ...current, crawled: event.target.value }))}
          >
            <option value="">{t('searchPage.fields.anyCrawled')}</option>
            <option value="true">{t('searchPage.fields.crawled')}</option>
            <option value="false">{t('searchPage.fields.notCrawled')}</option>
          </select>
          <Button type="submit">
            <Search />
            {t('searchPage.search')}
          </Button>
        </form>

        <SearchResults
          emptyText={hasSubmitted ? t('searchPage.empty') : t('searchPage.advanced.prompt')}
          isLoading={searchQuery.isLoading}
          page={searchQuery.data}
          onPageChange={(page) => setParams((current) => ({ ...current, page }))}
          onPageSizeChange={(size) => setParams((current) => ({ ...current, page: 0, size }))}
        />
      </CardContent>
    </Card>
  );
}

function ReindexPanel() {
  const { t } = useTranslation();
  const reindexMutation = useReindexNovelsMutation();

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex flex-wrap items-center gap-2">
          {t('searchPage.reindex.title')}
          <Badge>{t('searchPage.reindex.badge')}</Badge>
        </CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4">
        <p className="text-sm leading-6 text-muted-foreground">{t('searchPage.reindex.description')}</p>
        <Button className="w-fit" disabled={reindexMutation.isPending} type="button" onClick={() => reindexMutation.mutate()}>
          <RefreshCw className={reindexMutation.isPending ? 'animate-spin' : undefined} />
          {reindexMutation.isPending ? t('searchPage.reindex.running') : t('searchPage.reindex.action')}
        </Button>
        {reindexMutation.data ? (
          <pre className="overflow-x-auto rounded-lg border bg-muted/50 p-3 text-xs">
            {JSON.stringify(reindexMutation.data, null, 2)}
          </pre>
        ) : null}
      </CardContent>
    </Card>
  );
}

function SearchResults({
  emptyText,
  isLoading,
  onPageChange,
  onPageSizeChange,
  page,
}: {
  emptyText: string;
  isLoading: boolean;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  page?: PageResponse<Novel>;
}) {
  const { t } = useTranslation();
  const novels = page?.content ?? [];

  if (isLoading) {
    return <div className="grid min-h-48 place-items-center text-sm font-semibold text-muted-foreground">{t('searchPage.loading')}</div>;
  }

  return (
    <div className="grid gap-4">
      {!novels.length ? (
        <div className="rounded-lg border border-dashed p-5 text-sm text-muted-foreground">{emptyText}</div>
      ) : (
        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5">
          {novels.map((novel) => (
            <NovelCard key={novel.novelId} novel={novel} />
          ))}
        </section>
      )}
      <SearchPagination page={page} onPageChange={onPageChange} onPageSizeChange={onPageSizeChange} />
    </div>
  );
}

function SearchPagination({
  onPageChange,
  onPageSizeChange,
  page,
}: {
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
  page?: PageResponse<Novel>;
}) {
  const { t } = useTranslation();
  const currentPage = (page?.number ?? 0) + 1;
  const totalPages = Math.max(page?.totalPages ?? 1, 1);
  const pageSize = page?.size ?? 20;

  function goToPage(nextPage: number) {
    onPageChange(clampPage(nextPage, page?.totalPages ?? 0));
  }

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-t pt-4">
      <p className="text-sm text-muted-foreground">
        {t('novelList.pageInfo', { page: currentPage, total: totalPages })}
      </p>
      <div className="flex flex-wrap items-center gap-2">
        <label className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
          {t('crawlTasks.pageSize')}
          <Input
            className="h-9 w-20"
            min={1}
            type="number"
            value={pageSize}
            onChange={(event) => {
              const nextSize = Number(event.target.value);

              if (Number.isFinite(nextSize) && nextSize > 0) {
                onPageSizeChange(nextSize);
              }
            }}
          />
        </label>
        <Button disabled={page?.first ?? true} size="icon" type="button" variant="outline" onClick={() => goToPage(0)}>
          <ChevronFirst />
        </Button>
        <Button disabled={page?.first ?? true} size="icon" type="button" variant="outline" onClick={() => goToPage((page?.number ?? 0) - 1)}>
          <ChevronLeft />
        </Button>
        <label className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
          {t('novelList.page')}
          <Input
            className="h-9 w-20"
            min={1}
            max={totalPages}
            type="number"
            value={currentPage}
            onChange={(event) => {
              const nextPage = Number(event.target.value);

              if (Number.isFinite(nextPage) && nextPage > 0) {
                goToPage(nextPage - 1);
              }
            }}
          />
        </label>
        <Button disabled={page?.last ?? true} size="icon" type="button" variant="outline" onClick={() => goToPage((page?.number ?? 0) + 1)}>
          <ChevronRight />
        </Button>
        <Button disabled={page?.last ?? true} size="icon" type="button" variant="outline" onClick={() => goToPage((page?.totalPages ?? 1) - 1)}>
          <ChevronLast />
        </Button>
      </div>
    </div>
  );
}
