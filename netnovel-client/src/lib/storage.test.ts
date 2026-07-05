import { beforeEach, describe, expect, it } from 'vitest';

import { readStorage, writeStorage } from './storage';

describe('storage helpers', () => {
  // Generic JSON localStorage helpers used by reader settings and similar browser preferences.

  beforeEach(() => {
    window.localStorage.clear();
  });

  it('returns fallback when the key is missing', () => {
    expect(readStorage('missing-key', { theme: 'default' })).toEqual({ theme: 'default' });
  });

  it('returns fallback when stored JSON is invalid', () => {
    window.localStorage.setItem('broken-key', '{');

    expect(readStorage('broken-key', 'fallback')).toBe('fallback');
  });

  it('writes and reads JSON values', () => {
    writeStorage('settings', { fontSize: 'lg', background: 'sepia' });

    expect(readStorage('settings', null)).toEqual({ fontSize: 'lg', background: 'sepia' });
  });
});
