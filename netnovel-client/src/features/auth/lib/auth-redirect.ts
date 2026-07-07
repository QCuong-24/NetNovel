import type { Location } from 'react-router-dom';

type RedirectLocation = Pick<Location, 'hash' | 'pathname' | 'search'>;

export type AuthRedirectState = {
  from?: string | Partial<RedirectLocation> | null;
};

const authPaths = new Set(['/login', '/register']);

function isSafeInternalPath(path: string) {
  return path.startsWith('/') && !path.startsWith('//') && !authPaths.has(path.split(/[?#]/)[0]);
}

function locationToPath(location: Partial<RedirectLocation>) {
  const pathname = location.pathname ?? '/';
  const search = location.search ?? '';
  const hash = location.hash ?? '';

  return `${pathname}${search}${hash}`;
}

export function createAuthRedirectState(location: RedirectLocation): AuthRedirectState | undefined {
  const target = locationToPath(location);

  return isSafeInternalPath(target) ? { from: target } : undefined;
}

export function getAuthRedirectTarget(state: unknown, fallback: string) {
  const from = (state as AuthRedirectState | null)?.from;
  const target = typeof from === 'string' ? from : from ? locationToPath(from) : fallback;

  return isSafeInternalPath(target) ? target : fallback;
}
