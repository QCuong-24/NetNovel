import { useEffect, useMemo, useState } from 'react';
import { readStorage, writeStorage } from '@/lib/storage';
import {
  defaultReaderSettings,
  readerClassMaps,
  readerSettingsStorageKey,
} from '../lib/reader-settings';
import type { ReaderSettings } from '../types';

export function useReaderSettings() {
  const [settings, setSettings] = useState<ReaderSettings>(() =>
    readStorage(readerSettingsStorageKey, defaultReaderSettings),
  );

  useEffect(() => {
    writeStorage(readerSettingsStorageKey, settings);
  }, [settings]);

  const classes = useMemo(
    () => ({
      container: readerClassMaps.width[settings.width],
      content: [
        readerClassMaps.fontFamily[settings.fontFamily],
        readerClassMaps.fontSize[settings.fontSize],
        readerClassMaps.lineHeight[settings.lineHeight],
      ].join(' '),
      background: readerClassMaps.background[settings.background],
    }),
    [settings],
  );

  function updateSetting<TKey extends keyof ReaderSettings>(key: TKey, value: ReaderSettings[TKey]) {
    setSettings((current) => ({ ...current, [key]: value }));
  }

  return { settings, classes, updateSetting };
}
