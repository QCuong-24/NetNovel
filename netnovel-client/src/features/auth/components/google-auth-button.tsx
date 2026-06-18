import { GoogleLogin } from '@react-oauth/google';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { env } from '@/config/env';
import { routes } from '@/config/routes';
import { useGoogleLoginMutation } from '../hooks/use-auth';

type GoogleAuthButtonProps = {
  redirectTo?: string;
};

export function GoogleAuthButton({ redirectTo = routes.home }: GoogleAuthButtonProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const googleLoginMutation = useGoogleLoginMutation();

  if (!env.googleClientId) {
    return (
      <Button disabled type="button" variant="outline">
        <span className="font-bold">G</span>
        {t('auth.googleNotConfigured')}
      </Button>
    );
  }

  return (
    <div className="grid justify-items-center">
      <GoogleLogin
        onSuccess={async (credentialResponse) => {
          if (!credentialResponse.credential) {
            toast.error(t('auth.googleMissingToken'));
            return;
          }

          try {
            await googleLoginMutation.mutateAsync({ idToken: credentialResponse.credential });
            navigate(redirectTo, { replace: true });
          } catch {
            // Error toast is handled by the mutation.
          }
        }}
        onError={() => {
          toast.error(t('auth.googleFailed'));
        }}
        text="continue_with"
        theme="outline"
        width="320"
      />
      {googleLoginMutation.isPending ? (
        <p className="mt-2 text-xs font-semibold text-muted-foreground">{t('auth.googleSigningIn')}</p>
      ) : null}
    </div>
  );
}
