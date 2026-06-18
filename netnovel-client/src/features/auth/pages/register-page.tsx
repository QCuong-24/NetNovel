import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { routes } from '@/config/routes';
import { AuthCard } from '../components/auth-card';
import { GoogleAuthButton } from '../components/google-auth-button';
import { useRegisterMutation } from '../hooks/use-auth';

const registerSchema = z.object({
  username: z.string().min(3),
  email: z.string().email(),
  password: z.string().min(6),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const registerMutation = useRegisterMutation();
  const {
    formState: { errors },
    handleSubmit,
    register,
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
    },
  });

  async function onSubmit(values: RegisterFormValues) {
    try {
      await registerMutation.mutateAsync(values);
      navigate(routes.home);
    } catch {
      // Error toast is handled by the mutation.
    }
  }

  return (
    <AuthCard title={t('auth.register')} description={t('auth.registerDescription')}>
      <GoogleAuthButton />
      <div className="my-5 flex items-center gap-3 text-xs font-semibold uppercase text-muted-foreground">
        <div className="h-px flex-1 bg-border" />
        {t('auth.orEmail')}
        <div className="h-px flex-1 bg-border" />
      </div>
      <form className="grid gap-4" onSubmit={handleSubmit(onSubmit)}>
        <label className="grid gap-2 text-sm font-semibold">
          {t('auth.username')}
          <Input autoComplete="username" type="text" {...register('username')} />
          {errors.username ? (
            <span className="text-xs text-destructive">{t('auth.usernameMin')}</span>
          ) : null}
        </label>

        <label className="grid gap-2 text-sm font-semibold">
          {t('auth.email')}
          <Input autoComplete="email" type="email" {...register('email')} />
          {errors.email ? <span className="text-xs text-destructive">{t('auth.invalidEmail')}</span> : null}
        </label>

        <label className="grid gap-2 text-sm font-semibold">
          {t('auth.password')}
          <Input autoComplete="new-password" type="password" {...register('password')} />
          {errors.password ? (
            <span className="text-xs text-destructive">{t('auth.passwordMin')}</span>
          ) : null}
        </label>

        <Button disabled={registerMutation.isPending} type="submit">
          {registerMutation.isPending ? t('auth.creatingAccount') : t('auth.register')}
        </Button>
      </form>

      <p className="mt-5 text-center text-sm text-muted-foreground">
        {t('auth.hasAccount')}{' '}
        <Link className="font-semibold text-primary hover:underline" to={routes.login}>
          {t('auth.login')}
        </Link>
      </p>
    </AuthCard>
  );
}
