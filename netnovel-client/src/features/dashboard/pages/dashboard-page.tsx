import { type FormEvent, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Bell, ShieldCheck, Trash2, Users } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useCurrentUser } from '@/features/auth/hooks/use-auth';
import type { User } from '@/features/auth/types';
import { canManageNovels } from '@/features/novels/lib/novel-permissions';
import { formatDateTime } from '@/features/novels/lib/novel-format';
import { StatisticLineGraph } from '@/features/rankings/components/statistic-line-graph';
import {
  useAdminUsers,
  useCreateAdminUserMutation,
  useDeleteAdminUserMutation,
  useUpdateAdminUserMutation,
} from '@/features/admin-users/hooks/use-admin-users';
import { useSendNotificationToUserMutation } from '@/features/notifications/hooks/use-notifications';
import { useHashTab } from '@/hooks/use-hash-tab';
import { routes } from '@/config/routes';
import { cn } from '@/lib/utils';

type DashboardTab = 'statistic' | 'users';

const dashboardTabs: DashboardTab[] = ['statistic', 'users'];
const roles = ['USER', 'MANAGER', 'ADMIN'];

function isAdmin(user?: User) {
  return Boolean(user?.roles?.includes('ADMIN'));
}

function normalizeRoles(userRoles?: string[]) {
  return userRoles?.length ? userRoles : ['USER'];
}

function haveSameRoles(first: string[], second: string[]) {
  const firstSorted = [...first].sort();
  const secondSorted = [...second].sort();

  return firstSorted.length === secondSorted.length && firstSorted.every((role, index) => role === secondSorted[index]);
}

export function DashboardPage() {
  const { t } = useTranslation();
  const { data: user, isLoading } = useCurrentUser();
  const canAccessDashboard = canManageNovels(user);
  const canManageUsers = isAdmin(user);
  const [activeTab, setActiveTab] = useHashTab(dashboardTabs, 'statistic');

  useEffect(() => {
    if (!canManageUsers && activeTab === 'users') {
      setActiveTab('statistic');
    }
  }, [activeTab, canManageUsers, setActiveTab]);

  if (isLoading) {
    return <div className="grid min-h-64 place-items-center text-sm font-semibold text-muted-foreground">Loading...</div>;
  }

  if (!canAccessDashboard) {
    return (
      <PermissionCard
        title={t('dashboardPage.noPermission')}
        description={t('dashboardPage.noPermissionDescription')}
      />
    );
  }

  return (
    <div className="grid gap-6">
      <header className="grid gap-2">
        <p className="text-sm font-semibold uppercase text-primary">{t('dashboardPage.eyebrow')}</p>
        <h1 className="text-3xl font-extrabold tracking-normal md:text-4xl">{t('dashboardPage.title')}</h1>
        <p className="max-w-3xl text-sm leading-6 text-muted-foreground">{t('dashboardPage.description')}</p>
      </header>

      <div className="flex w-full flex-wrap gap-2 rounded-lg border bg-card p-1">
        <Button
          className="flex-1 sm:flex-none"
          type="button"
          variant={activeTab === 'statistic' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('statistic')}
        >
          <ShieldCheck />
          {t('dashboardPage.tabs.statistic')}
        </Button>
        {canManageUsers ? (
          <Button
            className="flex-1 sm:flex-none"
            type="button"
            variant={activeTab === 'users' ? 'default' : 'ghost'}
            onClick={() => setActiveTab('users')}
          >
            <Users />
            {t('dashboardPage.tabs.users')}
          </Button>
        ) : null}
      </div>

      {activeTab === 'statistic' ? <StatisticLineGraph /> : null}
      {activeTab === 'users' && canManageUsers ? <UserManagerPanel /> : null}
    </div>
  );
}

function PermissionCard({ description, title }: { description: string; title: string }) {
  return (
    <Card>
      <CardContent className="grid min-h-52 place-items-center gap-2 p-6 text-center">
        <div className="grid gap-2">
          <h2 className="text-xl font-extrabold">{title}</h2>
          <p className="max-w-xl text-sm leading-6 text-muted-foreground">{description}</p>
        </div>
      </CardContent>
    </Card>
  );
}

function UserManagerPanel() {
  const { t } = useTranslation();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const usersQuery = useAdminUsers({ page, size });
  const usersPage = usersQuery.data;

  return (
    <div className="grid gap-5">
      <CreateUserCard />

      <Card>
        <CardHeader className="flex-row items-center justify-between gap-3">
          <CardTitle className="flex items-center gap-2">
            <Users className="size-5 text-primary" />
            {t('dashboardPage.users.title')}
          </CardTitle>
          <label className="flex items-center gap-2 text-sm font-bold text-muted-foreground">
            {t('dashboardPage.users.pageSize')}
            <Input
              className="w-24"
              max="50"
              min="1"
              value={size}
              type="number"
              onChange={(event) => {
                setPage(0);
                setSize(Number(event.target.value) || 10);
              }}
            />
          </label>
        </CardHeader>
        <CardContent className="grid gap-3">
          {usersQuery.isLoading ? (
            <div className="grid min-h-40 place-items-center text-sm font-semibold text-muted-foreground">
              {t('dashboardPage.users.loading')}
            </div>
          ) : usersPage?.content.length ? (
            usersPage.content.map((user) => <UserRow key={user.userId} user={user} />)
          ) : (
            <div className="grid min-h-40 place-items-center text-sm font-semibold text-muted-foreground">
              {t('dashboardPage.users.empty')}
            </div>
          )}

          <div className="flex flex-col gap-2 border-t pt-3 sm:flex-row sm:items-center sm:justify-between">
            <span className="text-sm font-bold text-muted-foreground">
              {t('dashboardPage.users.pageInfo', {
                page: usersPage ? usersPage.number + 1 : 0,
                total: usersPage?.totalPages ?? 0,
              })}
            </span>
            <div className="flex gap-2">
              <Button
                disabled={(usersPage?.first ?? true) || usersQuery.isLoading}
                type="button"
                variant="outline"
                onClick={() => setPage((current) => Math.max(0, current - 1))}
              >
                {t('rankingPage.previous')}
              </Button>
              <Button
                disabled={(usersPage?.last ?? true) || usersQuery.isLoading}
                type="button"
                variant="outline"
                onClick={() => setPage((current) => current + 1)}
              >
                {t('rankingPage.next')}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function CreateUserCard() {
  const { t } = useTranslation();
  const createMutation = useCreateAdminUserMutation();
  const [selectedRoles, setSelectedRoles] = useState<string[]>(['USER']);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);

    createMutation.mutate({
      username: String(formData.get('username') ?? ''),
      email: String(formData.get('email') ?? ''),
      password: String(formData.get('password') ?? ''),
      roles: selectedRoles,
    });

    event.currentTarget.reset();
    setSelectedRoles(['USER']);
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('dashboardPage.users.create')}</CardTitle>
      </CardHeader>
      <CardContent>
        <form className="grid gap-3 md:grid-cols-[1fr_1fr_1fr_auto] md:items-end" onSubmit={handleSubmit}>
          <label className="grid gap-2 text-sm font-bold text-muted-foreground">
            {t('auth.username')}
            <Input name="username" required />
          </label>
          <label className="grid gap-2 text-sm font-bold text-muted-foreground">
            {t('auth.email')}
            <Input name="email" required type="email" />
          </label>
          <label className="grid gap-2 text-sm font-bold text-muted-foreground">
            {t('auth.password')}
            <Input name="password" required type="password" />
          </label>
          <Button disabled={createMutation.isPending} type="submit">
            {createMutation.isPending ? t('dashboardPage.users.creating') : t('dashboardPage.users.create')}
          </Button>
          <div className="flex flex-wrap gap-3 md:col-span-4">
            {roles.map((role) => (
              <RoleCheckbox key={role} role={role} selectedRoles={selectedRoles} onChange={setSelectedRoles} />
            ))}
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function UserRow({ user }: { user: User }) {
  const { t } = useTranslation();
  const updateMutation = useUpdateAdminUserMutation();
  const deleteMutation = useDeleteAdminUserMutation();
  const [showNotificationForm, setShowNotificationForm] = useState(false);
  const [selectedRoles, setSelectedRoles] = useState<string[]>(() => normalizeRoles(user.roles));
  const [savedRoles, setSavedRoles] = useState<string[]>(() => normalizeRoles(user.roles));
  const hasRoleChanges = !haveSameRoles(selectedRoles, savedRoles);

  useEffect(() => {
    const nextRoles = normalizeRoles(user.roles);
    setSelectedRoles(nextRoles);
    setSavedRoles(nextRoles);
  }, [user.roles]);

  return (
    <div className="grid gap-3 rounded-lg border bg-background p-3">
      <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_260px_auto] lg:items-center">
        <div className="flex min-w-0 items-center gap-3">
          <Link
            aria-label={`View ${user.username}'s profile`}
            className="grid size-11 shrink-0 place-items-center overflow-hidden rounded-full bg-primary text-sm font-extrabold text-primary-foreground transition-opacity hover:opacity-80"
            to={routes.userProfile(user.userId)}
          >
            {user.profilePictureUrl ? <img alt={user.username} className="size-full object-cover" src={user.profilePictureUrl} /> : user.username.slice(0, 2).toUpperCase()}
          </Link>
          <div className="min-w-0">
            <h3 className="truncate text-sm font-extrabold">{user.username}</h3>
            <p className="truncate text-sm font-semibold text-muted-foreground">{user.email}</p>
            <p className="text-xs font-semibold text-muted-foreground">{formatDateTime(user.createAt)}</p>
          </div>
        </div>

        <div className="flex flex-wrap gap-2">
          {roles.map((role) => (
            <RoleCheckbox
              key={role}
              role={role}
              savedRoles={savedRoles}
              selectedRoles={selectedRoles}
              onChange={setSelectedRoles}
            />
          ))}
        </div>

        <div className="flex flex-wrap gap-2 lg:justify-end">
          <Badge variant="secondary">{user.provider ?? 'LOCAL'}</Badge>
          <Button
            disabled={updateMutation.isPending || !hasRoleChanges}
            type="button"
            variant={hasRoleChanges ? 'default' : 'outline'}
            onClick={() =>
              updateMutation.mutate(
                { userId: String(user.userId), payload: { roles: selectedRoles } },
                { onSuccess: () => setSavedRoles(selectedRoles) },
              )
            }
          >
            {t('dashboardPage.users.saveRoles')}
          </Button>
          <Button
            size="icon"
            title={t('dashboardPage.users.sendNotification')}
            type="button"
            variant={showNotificationForm ? 'default' : 'outline'}
            onClick={() => setShowNotificationForm((current) => !current)}
          >
            <Bell />
          </Button>
          <Button
            disabled={deleteMutation.isPending}
            size="icon"
            title={t('dashboardPage.users.delete')}
            type="button"
            variant="destructive"
            onClick={() => deleteMutation.mutate(String(user.userId))}
          >
            <Trash2 />
          </Button>
        </div>
      </div>

      {showNotificationForm ? <AdminNotificationForm userId={String(user.userId)} onSent={() => setShowNotificationForm(false)} /> : null}
    </div>
  );
}

function AdminNotificationForm({ onSent, userId }: { onSent: () => void; userId: string }) {
  const { t } = useTranslation();
  const sendNotificationMutation = useSendNotificationToUserMutation();

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);

    sendNotificationMutation.mutate(
      {
        userId,
        payload: {
          type: String(formData.get('type') ?? '').trim(),
          title: String(formData.get('title') ?? '').trim(),
          message: String(formData.get('message') ?? '').trim(),
          link: String(formData.get('link') ?? '').trim() || undefined,
        },
      },
      { onSuccess: onSent },
    );
  }

  return (
    <form className="grid gap-3 rounded-lg border bg-card p-3 md:grid-cols-[160px_minmax(0,1fr)_minmax(0,1fr)]" onSubmit={handleSubmit}>
      <Input defaultValue="ADMIN_MESSAGE" name="type" placeholder={t('dashboardPage.users.notificationType')} required />
      <Input name="title" placeholder={t('dashboardPage.users.notificationTitle')} required />
      <Input name="link" placeholder={t('dashboardPage.users.notificationLink')} />
      <textarea
        className="min-h-24 resize-y rounded-md border bg-background px-3 py-2 text-sm leading-6 outline-none transition focus-visible:ring-2 focus-visible:ring-ring md:col-span-3"
        name="message"
        placeholder={t('dashboardPage.users.notificationMessage')}
        required
      />
      <div className="flex justify-end gap-2 md:col-span-3">
        <Button type="button" variant="ghost" onClick={onSent}>
          {t('novelForm.cancel')}
        </Button>
        <Button disabled={sendNotificationMutation.isPending} type="submit">
          {sendNotificationMutation.isPending ? t('dashboardPage.users.sendingNotification') : t('dashboardPage.users.sendNotification')}
        </Button>
      </div>
    </form>
  );
}

function RoleCheckbox({
  onChange,
  role,
  savedRoles,
  selectedRoles,
}: {
  onChange: (roles: string[]) => void;
  role: string;
  savedRoles?: string[];
  selectedRoles: string[];
}) {
  const checked = selectedRoles.includes(role);
  const saved = savedRoles?.includes(role) ?? checked;
  const pendingAdd = checked && !saved;
  const pendingRemove = !checked && saved;

  return (
    <label
      className={cn(
        'inline-flex cursor-pointer items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-extrabold transition',
        checked && saved && 'border-primary bg-primary text-primary-foreground',
        pendingAdd && 'border-amber-400 bg-amber-100 text-amber-950 dark:bg-amber-500/20 dark:text-amber-200',
        pendingRemove && 'border-destructive/60 bg-destructive/10 text-destructive line-through',
        !checked && !saved && 'bg-background text-muted-foreground hover:text-foreground',
      )}
    >
      <input
        checked={checked}
        className="sr-only"
        type="checkbox"
        onChange={(event) => {
          if (event.target.checked) {
            onChange([...selectedRoles, role]);
          } else {
            const nextRoles = selectedRoles.filter((item) => item !== role);
            onChange(nextRoles.length ? nextRoles : ['USER']);
          }
        }}
      />
      {role}
    </label>
  );
}
