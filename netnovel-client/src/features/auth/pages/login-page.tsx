import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { routes } from '@/config/routes';
import { AuthCard } from '../components/auth-card';
import { GoogleAuthButton } from '../components/google-auth-button';
import { useLoginMutation } from '../hooks/use-auth';

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const loginMutation = useLoginMutation();
  const redirectTo = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? routes.home;
  const {
    formState: { errors },
    handleSubmit,
    register,
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  async function onSubmit(values: LoginFormValues) {
    try {
      await loginMutation.mutateAsync(values);
      navigate(redirectTo, { replace: true });
    } catch {
      // Error toast is handled by the mutation.
    }
  }

  return (
    <AuthCard title={t('auth.login')} description={t('auth.loginDescription')}>
      <GoogleAuthButton redirectTo={redirectTo} />
      <div className="my-5 flex items-center gap-3 text-xs font-semibold uppercase text-muted-foreground">
        <div className="h-px flex-1 bg-border" />
        {t('auth.orEmail')}
        <div className="h-px flex-1 bg-border" />
      </div>
      <form className="grid gap-4" onSubmit={handleSubmit(onSubmit)}>
        <label className="grid gap-2 text-sm font-semibold">
          {t('auth.email')}
          <Input autoComplete="email" type="email" {...register('email')} />
          {errors.email ? <span className="text-xs text-destructive">{t('auth.invalidEmail')}</span> : null}
        </label>

        <label className="grid gap-2 text-sm font-semibold">
          {t('auth.password')}
          <Input autoComplete="current-password" type="password" {...register('password')} />
          {errors.password ? (
            <span className="text-xs text-destructive">{t('auth.passwordMin')}</span>
          ) : null}
        </label>

        <Button disabled={loginMutation.isPending} type="submit">
          {loginMutation.isPending ? t('auth.signingIn') : t('auth.login')}
        </Button>
      </form>

      <p className="mt-5 text-center text-sm text-muted-foreground">
        {t('auth.noAccount')}{' '}
        <Link className="font-semibold text-primary hover:underline" to={routes.register}>
          {t('auth.register')}
        </Link>
      </p>
    </AuthCard>
  );
}
