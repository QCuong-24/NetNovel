import type { ReaderSettings } from '../types';

export const readerSettingsStorageKey = 'netnovel-reader-settings';

export const defaultReaderSettings: ReaderSettings = {
  fontFamily: 'serif',
  fontSize: 'md',
  lineHeight: 'relaxed',
  width: 'medium',
  background: 'default',
};

export const readerClassMaps = {
  fontFamily: {
    sans: 'font-sans',
    serif: 'font-serif',
    mono: 'font-mono',
  },
  fontSize: {
    sm: 'text-base',
    md: 'text-lg',
    lg: 'text-xl',
    xl: 'text-2xl',
  },
  lineHeight: {
    comfortable: 'leading-7',
    relaxed: 'leading-8',
    spacious: 'leading-10',
  },
  width: {
    narrow: 'max-w-2xl',
    medium: 'max-w-3xl',
    wide: 'max-w-5xl',
  },
  background: {
    default: 'reader-bg-default',
    soft: 'reader-bg-soft',
    sepia: 'reader-bg-sepia',
    dim: 'reader-bg-dim',
  },
} as const;
