import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

export function useHashTab<T extends string>(tabs: readonly T[], defaultTab: T) {
  const location = useLocation();
  const navigate = useNavigate();
  const tabKey = useMemo(() => tabs.join('|'), [tabs]);

  function resolveHashTab(hash: string) {
    const rawValue = hash.replace(/^#/, '');
    const value = decodeURIComponent(rawValue) as T;

    return tabs.includes(value) ? value : defaultTab;
  }

  const [activeTab, setActiveTabState] = useState<T>(() => resolveHashTab(window.location.hash));

  useEffect(() => {
    setActiveTabState(resolveHashTab(location.hash));
    // tabKey intentionally tracks allowed tab changes without depending on array identity.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [defaultTab, location.hash, tabKey]);

  const setActiveTab = useCallback((tab: T) => {
    setActiveTabState(tab);
    navigate(
      {
        pathname: location.pathname,
        search: location.search,
        hash: tab,
      },
      { replace: false },
    );
  }, [location.pathname, location.search, navigate]);

  return [activeTab, setActiveTab] as const;
}
