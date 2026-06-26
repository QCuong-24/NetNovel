import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { buildRoutePath, rememberRoute } from '@/lib/navigation/route-history';

export function RouteHistoryTracker() {
  const location = useLocation();

  useEffect(() => {
    rememberRoute(buildRoutePath(location));
  }, [location]);

  return null;
}
