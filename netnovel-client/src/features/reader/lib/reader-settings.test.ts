import { describe, expect, it } from 'vitest';

import { defaultReaderSettings, readerClassMaps, readerSettingsStorageKey } from './reader-settings';

describe('reader settings', () => {
  // Reader defaults must always map to valid CSS classes before being persisted.

  it('defines stable defaults for first-time readers', () => {
    expect(defaultReaderSettings).toEqual({
      fontFamily: 'serif',
      fontSize: 'md',
      lineHeight: 'relaxed',
      width: 'medium',
      background: 'default',
    });
  });

  it('maps every default setting to a css class', () => {
    expect(readerClassMaps.fontFamily[defaultReaderSettings.fontFamily]).toBe('font-serif');
    expect(readerClassMaps.fontSize[defaultReaderSettings.fontSize]).toBe('text-lg');
    expect(readerClassMaps.lineHeight[defaultReaderSettings.lineHeight]).toBe('leading-8');
    expect(readerClassMaps.width[defaultReaderSettings.width]).toBe('max-w-3xl');
    expect(readerClassMaps.background[defaultReaderSettings.background]).toBe('reader-bg-default');
  });

  it('uses a namespaced storage key', () => {
    expect(readerSettingsStorageKey).toBe('netnovel-reader-settings');
  });
});
