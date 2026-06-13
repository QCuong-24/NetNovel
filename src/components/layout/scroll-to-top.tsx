import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

export function ScrollToTop() {
  const { pathname, search } = useLocation();

  useEffect(() => {
    window.scrollTo({ left: 0, top: 0, behavior: 'instant' });
  }, [pathname, search]);

  return null;
}
