import { ArrowLeft, Home, LibraryBig } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { routes } from '@/config/routes';

type AuthCardProps = {
  title: string;
  description: string;
  children: ReactNode;
};

export function AuthCard({ title, description, children }: AuthCardProps) {
  const navigate = useNavigate();

  return (
    <Card className="relative w-full max-w-md">
      <div className="absolute right-4 top-4 flex gap-1">
        <Button aria-label="Back" size="icon" title="Back" type="button" variant="ghost" onClick={() => navigate(-1)}>
          <ArrowLeft />
        </Button>
        <Button aria-label="Home" size="icon" title="Home" type="button" variant="ghost" asChild>
          <Link to={routes.home}>
            <Home />
          </Link>
        </Button>
      </div>
      <CardHeader className="gap-4">
        <Link className="flex items-center gap-2 font-extrabold text-primary" to={routes.home}>
          <LibraryBig className="size-6" />
          <span>NetNovel</span>
        </Link>
        <div className="grid gap-2">
          <CardTitle className="text-2xl">{title}</CardTitle>
          <p className="text-sm text-muted-foreground">{description}</p>
        </div>
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  );
}
