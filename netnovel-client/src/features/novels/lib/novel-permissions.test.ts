import { describe, expect, it } from 'vitest';

import type { User } from '@/features/auth/types';

import { canManageNovels } from './novel-permissions';

describe('canManageNovels', () => {
  // Novel management UI is visible only to manager/admin roles.

  it('allows admins and managers', () => {
    expect(canManageNovels(userWithRoles(['ADMIN']))).toBe(true);
    expect(canManageNovels(userWithRoles(['MANAGER']))).toBe(true);
  });

  it('rejects regular users and missing users', () => {
    expect(canManageNovels(userWithRoles(['USER']))).toBe(false);
    expect(canManageNovels(undefined)).toBe(false);
  });

  function userWithRoles(roles: string[]): User {
    return {
      userId: 1,
      username: 'reader',
      email: 'reader@example.com',
      roles,
    };
  }
});
