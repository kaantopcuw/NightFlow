import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { AuthProvider } from './auth/AuthProvider';
import { sessionFromAuthResponse, writeStoredSession } from './auth/session';
import { DEMO_AUTH_RESPONSE, DEMO_CREDENTIALS, DEMO_EVENTS } from './mocks/fixtures';
import { AppRoutes } from './routes';

function renderApp(initialPath: string) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[initialPath]}>
        <AppRoutes />
      </MemoryRouter>
    </AuthProvider>,
  );
}

function firstEventName(): string {
  const event = DEMO_EVENTS[0];
  if (event === undefined) {
    throw new Error('The fixtures must contain at least one event.');
  }
  return event.name;
}

describe('protected routing', () => {
  it('sends an anonymous visitor to the login screen instead of a data screen', async () => {
    renderApp('/events');

    // MSW is configured to fail on unhandled requests, so this also proves the
    // events screen never mounted and never issued an anonymous API call.
    expect(await screen.findByRole('button', { name: 'Sign in' })).toBeInTheDocument();
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });

  it('restores a stored session and renders the protected screen', async () => {
    writeStoredSession(sessionFromAuthResponse(DEMO_AUTH_RESPONSE));

    renderApp('/events');

    expect(await screen.findByText(firstEventName())).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Events' })).toBeInTheDocument();
  });

  it('signs a user in and lands on the events screen', async () => {
    const user = userEvent.setup();
    renderApp('/login');

    await user.type(await screen.findByLabelText('Email'), DEMO_CREDENTIALS.email);
    await user.type(screen.getByLabelText('Password'), DEMO_CREDENTIALS.password);
    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByText(firstEventName())).toBeInTheDocument();
  });

  it('shows the auth-service failure message and stays on the login screen', async () => {
    const user = userEvent.setup();
    renderApp('/login');

    await user.type(await screen.findByLabelText('Email'), DEMO_CREDENTIALS.email);
    await user.type(screen.getByLabelText('Password'), 'not-the-password');
    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Email veya şifre hatalı.');
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('returns to the login screen after signing out', async () => {
    const user = userEvent.setup();
    writeStoredSession(sessionFromAuthResponse(DEMO_AUTH_RESPONSE));

    renderApp('/events');
    await user.click(await screen.findByRole('button', { name: 'Sign out' }));

    expect(await screen.findByRole('button', { name: 'Sign in' })).toBeInTheDocument();
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });
});
