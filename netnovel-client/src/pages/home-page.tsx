import { BookOpen, Bookmark, MessageCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { AdSlot } from '@/features/ads/components/ad-slot';

const novels = [
  { title: 'The Archive Below', author: 'Mira Sol', category: 'Mystery', progress: 68 },
  { title: 'Crown of Static', author: 'D. K. Nguyen', category: 'Sci-Fi', progress: 31 },
  { title: 'Lantern Court', author: 'Ari Vale', category: 'Fantasy', progress: 92 },
];

const stats = [
  { labelKey: 'home.stats.reading', value: '12', icon: BookOpen },
  { labelKey: 'home.stats.bookmarks', value: '48', icon: Bookmark },
  { labelKey: 'home.stats.comments', value: '126', icon: MessageCircle },
];

export function HomePage() {
  const { t } = useTranslation();

  return (
    <main className="mx-auto grid w-full max-w-7xl gap-6 px-4 py-6 md:px-6">
      <section className="grid gap-6 rounded-lg bg-hero px-5 py-8 text-primary-foreground md:grid-cols-[1.2fr_0.8fr] md:px-8 md:py-10">
        <div className="grid content-center gap-4">
          <Badge className="w-fit bg-white/15 text-white hover:bg-white/20">
            {t('home.eyebrow')}
          </Badge>
          <h1 className="max-w-3xl text-4xl font-extrabold leading-tight tracking-normal md:text-6xl">
            {t('home.title')}
          </h1>
          <p className="max-w-2xl text-base text-white/80 md:text-lg">{t('home.subtitle')}</p>
          <div className="flex flex-wrap gap-3">
            <Button asChild variant="secondary">
              <Link to="/novels/1/chapters/1">{t('home.tryReader')}</Link>
            </Button>
            <Button asChild className="bg-white/10 text-white hover:bg-white/20" variant="ghost">
              <Link to="/rankings">{t('nav.rankings')}</Link>
            </Button>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-3 md:grid-cols-1">
          {stats.map((stat) => (
            <div key={stat.labelKey} className="rounded-lg bg-white/10 p-4">
              <stat.icon className="mb-4 size-5" />
              <strong className="block text-2xl">{stat.value}</strong>
              <span className="text-sm text-white/75">{t(stat.labelKey)}</span>
            </div>
          ))}
        </div>
      </section>

      <AdSlot slot="home_top_banner" />

      <section className="grid gap-4">
        <div className="flex items-center justify-between gap-3">
          <h2 className="text-xl font-bold">{t('home.continueReading')}</h2>
          <Button variant="outline" asChild>
            <Link to="/novels">{t('common.viewAll')}</Link>
          </Button>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {novels.map((novel) => (
            <Card key={novel.title}>
              <div className="h-40 rounded-t-lg bg-cover-gradient" />
              <CardHeader>
                <CardTitle>{novel.title}</CardTitle>
                <p className="text-sm text-muted-foreground">{novel.author}</p>
              </CardHeader>
              <CardContent className="grid gap-3">
                <Badge variant="secondary" className="w-fit">
                  {novel.category}
                </Badge>
                <div className="grid gap-1">
                  <div className="flex justify-between text-sm font-semibold">
                    <span>{t('home.progress')}</span>
                    <span>{novel.progress}%</span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-muted">
                    <div className="h-full bg-primary" style={{ width: `${novel.progress}%` }} />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>
    </main>
  );
}
