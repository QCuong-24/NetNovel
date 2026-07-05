import { describe, expect, it } from 'vitest';

import { cn } from './utils';

describe('cn', () => {
  // UI components rely on cn() to combine conditional classes and resolve Tailwind conflicts.

  it('joins truthy class names', () => {
    const isHidden = false;

    expect(cn('flex', isHidden && 'hidden', 'items-center')).toBe('flex items-center');
  });

  it('merges conflicting tailwind classes with the latest value winning', () => {
    expect(cn('px-2 text-sm', 'px-4', 'text-lg')).toBe('px-4 text-lg');
  });
});
