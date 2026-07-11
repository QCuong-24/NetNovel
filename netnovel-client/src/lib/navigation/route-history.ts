const CURRENT_ROUTE_STORAGE_KEY = 'netnovel.currentRoute';
const PREVIOUS_ROUTE_STORAGE_KEY = 'netnovel.previousRoute';
const ROUTE_STACK_STORAGE_KEY = 'netnovel.routeStack';
const MAX_ROUTE_STACK_LENGTH = 10;

export function buildRoutePath(location: { pathname: string; search: string; hash: string }) {
  return `${location.pathname}${location.search}${location.hash}`;
}

function readRouteStack() {
  try {
    const rawStack = sessionStorage.getItem(ROUTE_STACK_STORAGE_KEY);
    const parsedStack = rawStack ? JSON.parse(rawStack) : [];

    if (Array.isArray(parsedStack)) {
      return parsedStack.filter((route): route is string => typeof route === 'string');
    }

    return [];
  } catch {
    return [];
  }
}

function writeRouteStack(routeStack: string[]) {
  sessionStorage.setItem(ROUTE_STACK_STORAGE_KEY, JSON.stringify(routeStack.slice(-MAX_ROUTE_STACK_LENGTH)));
}

function getRoutePathname(route: string) {
  try {
    return new URL(route, window.location.origin).pathname;
  } catch {
    return '';
  }
}

function isSamePageRoute(left: string, right: string) {
  try {
    const leftUrl = new URL(left, window.location.origin);
    const rightUrl = new URL(right, window.location.origin);

    return leftUrl.pathname === rightUrl.pathname && leftUrl.search === rightUrl.search;
  } catch {
    return left === right;
  }
}

function isReaderRoute(route: string) {
  return /^\/novels\/[^/]+\/chapters\/\d+$/.test(getRoutePathname(route));
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

    const routeStack = readRouteStack();
    const seededRouteStack = routeStack.length ? routeStack : [sessionStorage.getItem(PREVIOUS_ROUTE_STORAGE_KEY), currentRoute]
      .filter((route): route is string => Boolean(route));
    const nextRouteStack = seededRouteStack.at(-1) === routePath ? seededRouteStack : [...seededRouteStack, routePath];
    writeRouteStack(nextRouteStack);
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

export function getPreviousNonReaderRoute(currentRoute: string) {
  const routeStack = readRouteStack();

  for (let index = routeStack.length - 2; index >= 0; index -= 1) {
    const route = routeStack[index];

    if (!route || isSamePageRoute(route, currentRoute) || isReaderRoute(route)) {
      continue;
    }

    return route;
  }

  return null;
}
