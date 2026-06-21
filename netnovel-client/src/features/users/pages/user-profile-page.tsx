import { CalendarDays, Shield, UserRound } from 'lucide-react';
import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@/components/ui/card';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { useUserProfile } from '../hooks/use-users';

export function UserProfilePage() {
  const { userId } = useParams();
  const userProfileQuery = useUserProfile(userId);
  const user = userProfileQuery.data;

  if (userProfileQuery.isLoading) {
    return (
      <main className="mx-auto grid w-full max-w-4xl gap-6 px-4 py-6 md:px-6">
        <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">Loading profile...</div>
      </main>
    );
  }

  if (userProfileQuery.isError || !user) {
    return (
      <main className="mx-auto grid w-full max-w-4xl gap-6 px-4 py-6 md:px-6">
        <Card>
          <CardContent className="p-6 font-semibold">User profile was not found.</CardContent>
        </Card>
      </main>
    );
  }

  return (
    <main className="mx-auto grid w-full max-w-4xl gap-6 px-4 py-6 md:px-6">
      <section className="grid gap-6 rounded-xl border bg-card p-6 sm:grid-cols-[160px_minmax(0,1fr)] sm:items-center">
        <div className="grid aspect-square size-32 place-items-center overflow-hidden rounded-full bg-primary text-4xl font-extrabold text-primary-foreground sm:size-40">
          {user.profilePictureUrl ? (
            <img alt={user.username} className="size-full object-cover" src={user.profilePictureUrl} />
          ) : (
            user.username.slice(0, 2).toUpperCase()
          )}
        </div>
        <div className="grid gap-2">
          <p className="text-sm font-semibold uppercase tracking-wide text-primary">User profile</p>
          <h1 className="break-words text-3xl font-extrabold tracking-normal md:text-5xl">{user.username}</h1>
          <div className="flex flex-wrap gap-2 pt-2 text-sm text-muted-foreground">
            <span className="flex items-center gap-2">
              <Shield className="size-4" />
              {user.roles?.join(', ') || 'USER'}
            </span>
            {user.createAt ? (
              <span className="flex items-center gap-2">
                <CalendarDays className="size-4" />
                Joined {formatDateTime(user.createAt)}
              </span>
            ) : null}
          </div>
        </div>
      </section>

      <Card>
        <CardContent className="flex items-center gap-3 p-5 text-sm text-muted-foreground">
          <UserRound className="size-5 text-primary" />
          Public profile information
        </CardContent>
      </Card>
    </main>
  );
}
