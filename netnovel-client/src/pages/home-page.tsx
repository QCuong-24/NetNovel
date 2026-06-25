import { ArrowLeft, ArrowRight, BookOpen, Bookmark, LibraryBig, SlidersHorizontal } from 'lucide-react';
import { useEffect, useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { AdSlot } from '@/features/ads/components/ad-slot';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import { useLastReading } from '@/features/collection/hooks/use-collection';
import { NovelCard } from '@/features/novels/components/novel-card';
import { NovelCover } from '@/features/novels/components/novel-cover';
import { useGenres, useNovelList } from '@/features/novels/hooks/use-novels';
import { useForYouRecommendations } from '@/features/recommendations/hooks/use-recommendations';

const HERO_INTERVAL = 7000;

function HomeSection({ title, to, children }: { title: string; to: string; children: ReactNode }) {
  const { t } = useTranslation();

  return (
    <section className="grid gap-4">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-xl font-bold md:text-2xl">{title}</h2>
        <Button asChild size="sm" variant="outline">
          <Link to={to}>{t('common.viewAll')}</Link>
        </Button>
      </div>
      {children}
    </section>
  );
}

export function HomePage() {
  const { t } = useTranslation();
  const { data: user } = useCurrentUser();
  const [activeSlide, setActiveSlide] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [reduceMotion, setReduceMotion] = useState(false);
  const lastReadingQuery = useLastReading(3, Boolean(user));
  const recommendationsQuery = useForYouRecommendations(6, Boolean(user));
  const hotNovelsQuery = useNovelList({ kind: 'hot', page: 0, size: 6 });
  const newestNovelsQuery = useNovelList({ kind: 'newest', page: 0, size: 6 });
  const completedNovelsQuery = useNovelList({ kind: 'completed', page: 0, size: 6 });
  const genresQuery = useGenres();
  const lastReads = lastReadingQuery.data?.content ?? [];
  const recommendations = recommendationsQuery.data ?? [];
  const slides = [
    {
      icon: LibraryBig,
      eyebrow: t('home.slides.discover.eyebrow'),
      title: t('home.slides.discover.title'),
      description: t('home.slides.discover.description'),
      action: t('home.slides.discover.action'),
      to: '/novels',
    },
    {
      icon: SlidersHorizontal,
      eyebrow: t('home.slides.reader.eyebrow'),
      title: t('home.slides.reader.title'),
      description: t('home.slides.reader.description'),
      action: t('home.slides.reader.action'),
      to: '/novels',
    },
    {
      icon: Bookmark,
      eyebrow: t('home.slides.collection.eyebrow'),
      title: t('home.slides.collection.title'),
      description: t('home.slides.collection.description'),
      action: t('home.slides.collection.action'),
      to: user ? '/collection' : '/login',
    },
  ];
  const slide = slides[activeSlide];

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    const syncMotionPreference = () => setReduceMotion(mediaQuery.matches);
    syncMotionPreference();
    mediaQuery.addEventListener('change', syncMotionPreference);
    return () => mediaQuery.removeEventListener('change', syncMotionPreference);
  }, []);

  useEffect(() => {
    if (isPaused || reduceMotion) return undefined;
    const timer = window.setInterval(() => setActiveSlide((current) => (current + 1) % slides.length), HERO_INTERVAL);
    return () => window.clearInterval(timer);
  }, [isPaused, reduceMotion, slides.length]);

  function showSlide(index: number) {
    setActiveSlide((index + slides.length) % slides.length);
  }

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-8 px-4 py-6 md:px-6 md:py-8">
      <section
        aria-label={t('home.heroLabel')}
        aria-roledescription="carousel"
        className="relative overflow-hidden rounded-xl bg-hero text-primary-foreground shadow-sm"
        onBlur={(event) => {
          if (!event.currentTarget.contains(event.relatedTarget)) setIsPaused(false);
        }}
        onFocus={() => setIsPaused(true)}
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
      >
        <div className="pointer-events-none absolute -right-12 -top-20 size-72 rounded-full bg-white/10 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-28 left-1/3 size-72 rounded-full bg-black/10 blur-3xl" />
        <div className="relative grid min-h-80 gap-8 px-6 py-8 md:grid-cols-[1.35fr_0.65fr] md:px-10 md:py-12">
          <div className="grid content-center gap-5">
            <Badge className="w-fit bg-white/15 text-white hover:bg-white/20">{slide.eyebrow}</Badge>
            <h1 className="max-w-3xl text-4xl font-extrabold leading-tight tracking-normal md:text-6xl">{slide.title}</h1>
            <p className="max-w-2xl text-base leading-7 text-white/85 md:text-lg">{slide.description}</p>
            <div className="flex flex-wrap gap-3">
              <Button asChild variant="secondary"><Link to={slide.to}>{slide.action}</Link></Button>
            </div>
          </div>
          <div className="grid place-items-center">
            <div className="grid size-40 place-items-center rounded-[2rem] border border-white/20 bg-white/10 shadow-2xl backdrop-blur-sm md:size-52">
              <slide.icon className="size-16 md:size-20" strokeWidth={1.5} />
            </div>
          </div>
        </div>
        <div className="absolute inset-x-0 bottom-0 flex items-center justify-between gap-3 px-4 py-4 md:px-6">
          <div className="flex gap-2">
            {slides.map((item, index) => (
              <button key={item.eyebrow} aria-current={activeSlide === index ? 'true' : undefined} aria-label={t('home.showSlide', { number: index + 1 })} className={`h-2 rounded-full transition-all ${activeSlide === index ? 'w-7 bg-white' : 'w-2 bg-white/45 hover:bg-white/70'}`} type="button" onClick={() => showSlide(index)} />
            ))}
          </div>
          <div className="flex gap-2">
            <Button aria-label={t('home.previousSlide')} className="border-white/20 bg-white/10 text-white hover:bg-white/20" size="icon" type="button" variant="outline" onClick={() => showSlide(activeSlide - 1)}><ArrowLeft /></Button>
            <Button aria-label={t('home.nextSlide')} className="border-white/20 bg-white/10 text-white hover:bg-white/20" size="icon" type="button" variant="outline" onClick={() => showSlide(activeSlide + 1)}><ArrowRight /></Button>
          </div>
        </div>
      </section>

      <AdSlot slot="home_top_banner" />

      {user && lastReads.length ? (
        <HomeSection title={t('home.continueReading')} to="/collection">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {lastReads.map((lastRead) => {
              const canRead = Boolean(lastRead.novelId && lastRead.chapterId);
              const link = canRead ? `/novels/${lastRead.novelId}/chapters/${lastRead.chapterId}` : '/collection';
              return (
                <Card key={lastRead.lastReadId} className="overflow-hidden">
                  <CardContent className="grid grid-cols-[5rem_minmax(0,1fr)] gap-4 p-3">
                    <NovelCover className="h-28" src={lastRead.coverImageUrl} title={lastRead.novelTitle ?? t('home.untitledNovel')} />
                    <div className="grid content-center gap-2">
                      <p className="line-clamp-2 font-bold">{lastRead.novelTitle ?? t('home.untitledNovel')}</p>
                      <p className="line-clamp-1 text-sm text-muted-foreground">{lastRead.author}</p>
                      <p className="text-sm font-semibold text-primary">{lastRead.chapterNumber ? t('novelPages.latestChapter', { number: lastRead.chapterNumber }) : lastRead.chapterTitle}</p>
                      <Button asChild className="w-fit" size="sm"><Link to={link}><BookOpen />{t('home.resume')}</Link></Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </HomeSection>
      ) : null}

      {user && recommendations.length ? (
        <HomeSection title={t('home.recommendedForYou')} to="/collection#recommendations">
          <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-6">
            {recommendations.map((item) => <NovelCard key={item.novel.novelId} novel={item.novel} />)}
          </div>
        </HomeSection>
      ) : null}

      <HomeSection title={t('home.hotNovels')} to="/novels/hot">
        {hotNovelsQuery.data?.content.length ? <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-6">{hotNovelsQuery.data.content.map((novel) => <NovelCard key={novel.novelId} novel={novel} />)}</div> : null}
      </HomeSection>
      <HomeSection title={t('home.latestUpdates')} to="/novels/newest">
        {newestNovelsQuery.data?.content.length ? <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-6">{newestNovelsQuery.data.content.map((novel) => <NovelCard key={novel.novelId} novel={novel} />)}</div> : null}
      </HomeSection>
      <HomeSection title={t('home.completedNovels')} to="/novels/completed">
        {completedNovelsQuery.data?.content.length ? <div className="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-6">{completedNovelsQuery.data.content.map((novel) => <NovelCard key={novel.novelId} novel={novel} />)}</div> : null}
      </HomeSection>

      {genresQuery.data?.length ? (
        <section className="grid gap-4">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-xl font-bold md:text-2xl">{t('home.browseGenres')}</h2>
            <Button asChild size="sm" variant="outline"><Link to="/novels">{t('common.viewAll')}</Link></Button>
          </div>
          <div className="flex flex-wrap gap-2">
            {genresQuery.data.slice(0, 16).map((genre) => <Button key={genre.genreId} asChild variant="secondary"><Link to={`/novels/genres/${encodeURIComponent(genre.name)}`}>{genre.name}</Link></Button>)}
          </div>
        </section>
      ) : null}
    </main>
  );
}
