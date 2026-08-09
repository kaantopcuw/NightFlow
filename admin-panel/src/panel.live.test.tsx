/**
 * Live rendering suite — mounts the real route tree against a running backend,
 * with no MSW in the way. `gateway.live.test.ts` proves the API contract; this
 * file proves the screens survive the payloads that contract produces.
 *
 * Excluded from `npm test`. Run with `npm run test:live` (see README.md).
 */
import { cleanup, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it } from 'vitest';
import { setAuthToken } from './api/client';
import { login } from './api/endpoints';
import { AuthProvider } from './auth/AuthProvider';
import { sessionFromAuthResponse, writeStoredSession } from './auth/session';
import { AppRoutes } from './routes';

const EMAIL = import.meta.env.VITE_LIVE_EMAIL ?? 'reviewbot@example.com';
const PASSWORD = import.meta.env.VITE_LIVE_PASSWORD ?? 'test1234';

/** A real gateway round trip is slower than an MSW handler. */
const WAIT = { timeout: 20_000 };

function renderApp(initialPath: string) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[initialPath]}>
        <AppRoutes />
      </MemoryRouter>
    </AuthProvider>,
  );
}

async function seedStoredSession(): Promise<void> {
  const response = await login({ email: EMAIL, password: PASSWORD });
  writeStoredSession(sessionFromAuthResponse(response));
}

// The live suite runs without `src/test/setup.ts`, so unmounting is manual.
afterEach(() => {
  cleanup();
  setAuthToken(null);
  window.sessionStorage.clear();
});

describe('the panel against a live gateway', () => {
  it('signs in through the login form and renders the live event catalogue', async () => {
    const user = userEvent.setup();
    renderApp('/events'); // anonymous, so ProtectedRoute redirects to /login

    await user.type(await screen.findByLabelText('Email'), EMAIL);
    await user.type(screen.getByLabelText('Password'), PASSWORD);
    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    const table = await screen.findByRole('table', undefined, WAIT);
    // Header row plus at least one live event.
    expect(within(table).getAllByRole('row').length).toBeGreaterThan(1);
    expect(screen.getByText(/event\(s\)/)).toBeDefined();
  });

  it('renders the live venue list for a restored session', async () => {
    await seedStoredSession();
    renderApp('/venues');

    const table = await screen.findByRole('table', undefined, WAIT);
    expect(within(table).getAllByRole('row').length).toBeGreaterThan(1);
    expect(screen.getByRole('columnheader', { name: 'Capacity' })).toBeDefined();
  });

  it('renders the tickets screen — rows or the empty state, never an error', async () => {
    await seedStoredSession();
    renderApp('/tickets');

    // ticket-service has no user-facing way to create a SOLD ticket (see
    // README, "Not verified"), so an empty list is the expected outcome here.
    const outcome = await screen.findByText(
      /Ticket code|This account holds no tickets\./,
      undefined,
      WAIT,
    );
    expect(outcome).toBeDefined();
    expect(screen.queryByRole('alert')).toBeNull();
  });

  it('sends an unauthenticated visitor to the login screen', async () => {
    renderApp('/venues');

    expect(await screen.findByRole('button', { name: 'Sign in' })).toBeDefined();
    expect(screen.queryByRole('table')).toBeNull();
  });
});
