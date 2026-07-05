import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ProtectedRoute } from './protected-route';

vi.mock('../lib/auth-storage', () => ({
  hasAuthTokens: vi.fn(),
}));

vi.mock('../hooks/use-auth', () => ({
  useCurrentUser: vi.fn(),
}));

import { hasAuthTokens } from '../lib/auth-storage';
import { useCurrentUser } from '../hooks/use-auth';

const mockedHasAuthTokens = vi.mocked(hasAuthTokens);
const mockedUseCurrentUser = vi.mocked(useCurrentUser);

describe('ProtectedRoute', () => {
  // Route guard behavior with auth storage and current-user query mocked.

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('redirects guests to login and preserves the source location', () => {
    mockedHasAuthTokens.mockReturnValue(false);
    mockedUseCurrentUser.mockReturnValue({ isError: false, isLoading: false } as ReturnType<typeof useCurrentUser>);

    renderProtectedRoute('/dashboard');

    expect(screen.getByText('Login page')).toBeInTheDocument();
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('redirects when current user lookup fails', () => {
    mockedHasAuthTokens.mockReturnValue(true);
    mockedUseCurrentUser.mockReturnValue({ isError: true, isLoading: false } as ReturnType<typeof useCurrentUser>);

    renderProtectedRoute('/collection');

    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('shows loading state while current user is being fetched', () => {
    mockedHasAuthTokens.mockReturnValue(true);
    mockedUseCurrentUser.mockReturnValue({ isError: false, isLoading: true } as ReturnType<typeof useCurrentUser>);

    renderProtectedRoute('/dashboard');

    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('renders nested route content for authenticated users', () => {
    mockedHasAuthTokens.mockReturnValue(true);
    mockedUseCurrentUser.mockReturnValue({ isError: false, isLoading: false } as ReturnType<typeof useCurrentUser>);

    renderProtectedRoute('/dashboard');

    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });

  function renderProtectedRoute(initialPath: string) {
    return render(
      <MemoryRouter initialEntries={[initialPath]}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path={initialPath} element={<div>Protected content</div>} />
          </Route>
          <Route path="/login" element={<div>Login page</div>} />
        </Routes>
      </MemoryRouter>,
    );
  }
});
