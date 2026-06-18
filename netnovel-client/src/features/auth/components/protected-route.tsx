import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { routes } from '@/config/routes';
import { hasAuthTokens } from '../lib/auth-storage';
import { useCurrentUser } from '../hooks/use-auth';

export function ProtectedRoute() {
  const location = useLocation();
  const hasTokens = hasAuthTokens();
  const { isError, isLoading } = useCurrentUser();

  if (!hasTokens || isError) {
    return <Navigate replace to={routes.login} state={{ from: location }} />;
  }

  if (isLoading) {
    return (
      <div className="grid min-h-64 place-items-center px-4 text-sm font-semibold text-muted-foreground">
        Loading...
      </div>
    );
  }

  return <Outlet />;
}
