import { BookOpen, Bookmark, ChevronLeft, ChevronRight, LibraryBig, SlidersHorizontal } from 'lucide-react';
import { useEffect, useRef, useState, type ReactNode } from 'react';
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
  const touchStartXRef = useRef<number | null>(null);
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
      background: 'from-slate-950 via-indigo-950 to-sky-800',
      glow: 'bg-sky-300/20',
      eyebrow: t('home.slides.discover.eyebrow'),
      title: t('home.slides.discover.title'),
      description: t('home.slides.discover.description'),
      action: t('home.slides.discover.action'),
      to: '/novels',
    },
    {
      icon: SlidersHorizontal,
      background: 'from-zinc-950 via-violet-950 to-fuchsia-800',
      glow: 'bg-fuchsia-300/20',
      eyebrow: t('home.slides.reader.eyebrow'),
      title: t('home.slides.reader.title'),
      description: t('home.slides.reader.description'),
      action: t('home.slides.reader.action'),
      to: '/novels',
    },
    {
      icon: Bookmark,
      background: 'from-emerald-950 via-teal-900 to-cyan-800',
      glow: 'bg-emerald-300/20',
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

  function handleTouchEnd(clientX: number) {
    if (touchStartXRef.current === null) {
      return;
    }

    const distance = clientX - touchStartXRef.current;
    touchStartXRef.current = null;

    if (Math.abs(distance) < 44) {
      return;
    }

    showSlide(activeSlide + (distance < 0 ? 1 : -1));
  }

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-8 px-4 py-6 md:px-6 md:py-8">
      <section
        aria-label={t('home.heroLabel')}
        aria-roledescription="carousel"
        className={`relative h-[18rem] overflow-hidden rounded-xl bg-gradient-to-br text-white shadow-sm sm:h-[20rem] md:h-[24rem] ${slide.background}`}
        onBlur={(event) => {
          if (!event.currentTarget.contains(event.relatedTarget)) setIsPaused(false);
        }}
        onFocus={() => setIsPaused(true)}
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
        onTouchCancel={() => {
          touchStartXRef.current = null;
          setIsPaused(false);
        }}
        onTouchEnd={(event) => {
          handleTouchEnd(event.changedTouches[0]?.clientX ?? 0);
          setIsPaused(false);
        }}
        onTouchStart={(event) => {
          touchStartXRef.current = event.touches[0]?.clientX ?? null;
          setIsPaused(true);
        }}
      >
        <div className="pointer-events-none absolute -right-12 -top-20 size-72 rounded-full bg-white/10 blur-3xl" />
        <div className={`pointer-events-none absolute -bottom-28 left-1/3 size-72 rounded-full ${slide.glow} blur-3xl`} />
        <div key={`mobile-icon-${activeSlide}`} className="hero-slide-motion pointer-events-none absolute -bottom-5 -right-4 z-0 grid size-32 place-items-center rounded-[2rem] border border-white/10 bg-white/5 opacity-15 backdrop-blur-sm sm:size-40 md:hidden">
          <slide.icon className="size-16" strokeWidth={1.5} />
        </div>
        <div className="relative z-10 grid h-full gap-5 px-5 py-6 pb-12 md:grid-cols-[1.35fr_0.65fr] md:px-12 md:py-10">
          <div key={`content-${activeSlide}`} className="hero-slide-motion grid max-w-3xl content-center gap-3 sm:gap-4">
            <Badge className="w-fit border-white/20 bg-white/15 text-white hover:bg-white/20">{slide.eyebrow}</Badge>
            <h1 className="max-w-3xl text-3xl font-extrabold leading-tight tracking-normal text-white sm:text-4xl md:text-6xl">{slide.title}</h1>
            <p className="max-w-2xl text-sm leading-6 text-white/85 sm:text-base md:text-lg md:leading-7">{slide.description}</p>
            <div className="flex flex-wrap gap-3">
              <Button asChild className="bg-white !text-slate-950 shadow-sm hover:bg-white/90 hover:!text-slate-950">
                <Link className="!text-slate-950" to={slide.to}>{slide.action}</Link>
              </Button>
            </div>
          </div>
          <div key={`desktop-icon-${activeSlide}`} className="hero-slide-motion hidden place-items-center md:grid">
            <div className="grid size-44 place-items-center rounded-[2rem] border border-white/20 bg-white/10 shadow-2xl backdrop-blur-sm lg:size-52">
              <slide.icon className="size-16 text-white md:size-20" strokeWidth={1.5} />
            </div>
          </div>
        </div>
        <div className="absolute inset-x-0 bottom-3 flex justify-center px-4">
          <div className="flex rounded-full border border-white/10 bg-black/15 px-2.5 py-2 backdrop-blur-sm">
            {slides.map((item, index) => (
              <button key={item.eyebrow} aria-current={activeSlide === index ? 'true' : undefined} aria-label={t('home.showSlide', { number: index + 1 })} className={`h-2 rounded-full transition-all ${activeSlide === index ? 'w-7 bg-white' : 'w-2 bg-white/45 hover:bg-white/70'}`} type="button" onClick={() => showSlide(index)} />
            ))}
          </div>
        </div>
        <Button aria-label={t('home.previousSlide')} className="absolute left-4 top-1/2 z-20 hidden -translate-y-1/2 border-0 bg-transparent text-white/75 shadow-none hover:bg-white/10 hover:text-white md:inline-flex" size="icon" type="button" variant="ghost" onClick={() => showSlide(activeSlide - 1)}><ChevronLeft className="size-8 text-white" strokeWidth={2.25} /></Button>
        <Button aria-label={t('home.nextSlide')} className="absolute right-4 top-1/2 z-20 hidden -translate-y-1/2 border-0 bg-transparent text-white/75 shadow-none hover:bg-white/10 hover:text-white md:inline-flex" size="icon" type="button" variant="ghost" onClick={() => showSlide(activeSlide + 1)}><ChevronRight className="size-8 text-white" strokeWidth={2.25} /></Button>
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
