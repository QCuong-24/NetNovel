const CURRENT_ROUTE_STORAGE_KEY = 'netnovel.currentRoute';
const PREVIOUS_ROUTE_STORAGE_KEY = 'netnovel.previousRoute';

export function buildRoutePath(location: { pathname: string; search: string; hash: string }) {
  return `${location.pathname}${location.search}${location.hash}`;
}

export function rememberRoute(routePath: string) {
  try {
    const currentRoute = sessionStorage.getItem(CURRENT_ROUTE_STORAGE_KEY);

    if (currentRoute === routePath) {
      return;
    }

    if (currentRoute) {
      sessionStorage.setItem(PREVIOUS_ROUTE_STORAGE_KEY, currentRoute);
    } else {
      sessionStorage.removeItem(PREVIOUS_ROUTE_STORAGE_KEY);
    }

    sessionStorage.setItem(CURRENT_ROUTE_STORAGE_KEY, routePath);
  } catch {
    // sessionStorage can be unavailable in strict privacy modes.
  }
}

export function getPreviousRoute() {
  try {
    return sessionStorage.getItem(PREVIOUS_ROUTE_STORAGE_KEY);
  } catch {
    return null;
  }
}
