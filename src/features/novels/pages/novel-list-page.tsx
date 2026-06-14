import { ChevronFirst, ChevronLast, ChevronLeft, ChevronRight } from 'lucide-react';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NovelCard } from '../components/novel-card';
import { useNovelList } from '../hooks/use-novels';
import type { NovelListKind } from '../types';

type NovelListPageProps = {
  kind: NovelListKind;
};

function clampPage(page: number, totalPages: number) {
  if (totalPages <= 0) {
    return 0;
  }

  return Math.min(Math.max(page, 0), totalPages - 1);
}

export function NovelListPage({ kind }: NovelListPageProps) {
  const { t } = useTranslation();
  const { genreName } = useParams();
  const decodedGenreName = useMemo(() => (genreName ? decodeURIComponent(genreName) : undefined), [genreName]);
  const [page, setPage] = useState(0);
  const [pageInput, setPageInput] = useState('1');
  const novelsQuery = useNovelList({
    kind,
    genreName: decodedGenreName,
    page,
    size: 20,
  });
  const novelPage = novelsQuery.data;
  const novels = novelPage?.content ?? [];
  const currentPage = (novelPage?.number ?? page) + 1;
  const totalPages = Math.max(novelPage?.totalPages ?? 1, 1);

  function goToPage(nextPage: number) {
    const clampedPage = clampPage(nextPage, novelPage?.totalPages ?? 0);
    setPage(clampedPage);
    setPageInput(String(clampedPage + 1));
  }

  const title = decodedGenreName
    ? t('novelList.genreTitle', { genre: decodedGenreName })
    : t(`novelList.titles.${kind}`);

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <header className="grid gap-2">
        <p className="text-sm font-semibold uppercase text-primary">{t('nav.library')}</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-5xl">{title}</h1>
        <p className="max-w-3xl text-sm leading-6 text-muted-foreground">{t(`novelList.descriptions.${kind}`)}</p>
      </header>

      {novelsQuery.isLoading ? (
        <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">
          {t('novelList.loading')}
        </div>
      ) : null}

      {!novelsQuery.isLoading && !novels.length ? (
        <div className="rounded-lg border border-dashed p-6 text-sm text-muted-foreground">{t('novelList.empty')}</div>
      ) : null}

      {novels.length ? (
        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5">
          {novels.map((novel) => (
            <NovelCard key={novel.novelId} novel={novel} />
          ))}
        </section>
      ) : null}

      <div className="flex flex-wrap items-center justify-between gap-3 border-t pt-4">
        <p className="text-sm text-muted-foreground">
          {t('novelList.pageInfo', { page: currentPage, total: totalPages })}
        </p>
        <div className="flex flex-wrap items-center gap-2">
          <Button
            aria-label={t('novelList.firstPage')}
            disabled={novelPage?.first ?? true}
            size="icon"
            type="button"
            variant="outline"
            onClick={() => goToPage(0)}
          >
            <ChevronFirst />
          </Button>
          <Button
            aria-label={t('common.back')}
            disabled={novelPage?.first ?? true}
            size="icon"
            type="button"
            variant="outline"
            onClick={() => goToPage(page - 1)}
          >
            <ChevronLeft />
          </Button>
          <label className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
            {t('novelList.page')}
            <Input
              className="h-9 w-20"
              min={1}
              max={totalPages}
              type="number"
              value={pageInput}
              onBlur={() => goToPage(Number(pageInput) - 1)}
              onChange={(event) => setPageInput(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  goToPage(Number(pageInput) - 1);
                }
              }}
            />
          </label>
          <Button
            aria-label={t('chapters.next')}
            disabled={novelPage?.last ?? true}
            size="icon"
            type="button"
            variant="outline"
            onClick={() => goToPage(page + 1)}
          >
            <ChevronRight />
          </Button>
          <Button
            aria-label={t('novelList.lastPage')}
            disabled={novelPage?.last ?? true}
            size="icon"
            type="button"
            variant="outline"
            onClick={() => goToPage((novelPage?.totalPages ?? 1) - 1)}
          >
            <ChevronLast />
          </Button>
        </div>
      </div>
    </main>
  );
}
