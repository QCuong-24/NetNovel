import {
  useCallback,
  useEffect,
  useLayoutEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { ThemeContext, type AppTheme } from './theme-context';

type ThemeProviderProps = {
  children: ReactNode;
  defaultTheme?: AppTheme;
  storageKey?: string;
};

const preferencesStorageVersionKey = 'netnovel-preferences-storage-version';
const currentPreferencesStorageVersion = '3';
const readerSettingsStorageKey = 'netnovel-reader-settings';

const useIsomorphicLayoutEffect = typeof window === 'undefined' ? useEffect : useLayoutEffect;

function readInitialTheme(storageKey: string, defaultTheme: AppTheme): AppTheme {
  if (typeof window === 'undefined') {
    return defaultTheme;
  }

  const storageVersion = window.localStorage.getItem(preferencesStorageVersionKey);

  if (storageVersion !== currentPreferencesStorageVersion) {
    window.localStorage.removeItem(storageKey);
    window.localStorage.removeItem(readerSettingsStorageKey);
    window.localStorage.setItem(preferencesStorageVersionKey, currentPreferencesStorageVersion);

    return defaultTheme;
  }

  const storedTheme = window.localStorage.getItem(storageKey);

  return storedTheme === 'dark' || storedTheme === 'light' ? storedTheme : defaultTheme;
}

function applyTheme(theme: AppTheme, storageKey: string) {
  const root = document.documentElement;

  root.classList.remove('light', 'dark');
  root.classList.add(theme);
  root.dataset.theme = theme;
  root.style.colorScheme = theme;
  window.localStorage.setItem(storageKey, theme);
}

export function ThemeProvider({
  children,
  defaultTheme = 'light',
  storageKey = 'netnovel-theme',
}: ThemeProviderProps) {
  const [theme, setThemeState] = useState<AppTheme>(() => readInitialTheme(storageKey, defaultTheme));

  useIsomorphicLayoutEffect(() => {
    applyTheme(theme, storageKey);
  }, [storageKey, theme]);

  const setTheme = useCallback((nextTheme: AppTheme) => {
    setThemeState(nextTheme);
  }, []);

  const toggleTheme = useCallback(() => {
    setThemeState((currentTheme) => (currentTheme === 'dark' ? 'light' : 'dark'));
  }, []);

  const value = useMemo(
    () => ({
      theme,
      setTheme,
      toggleTheme,
    }),
    [setTheme, theme, toggleTheme],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
