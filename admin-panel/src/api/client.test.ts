import { HttpResponse, http } from 'msw';
import { describe, expect, it, vi } from 'vitest';
import { DEMO_CREDENTIALS } from '../mocks/fixtures';
import { server } from '../test/server';
import { ApiError, getAuthToken, setAuthToken, setUnauthorizedHandler } from './client';
import { API_ROUTES, listEvents, listVenues, login } from './endpoints';

const BASE = import.meta.env.VITE_API_BASE_URL ?? '';

function urlOf(path: string): string {
  return `${BASE}${path}`;
}

describe('login', () => {
  it('returns the auth-service payload for valid credentials', async () => {
    const response = await login({
      email: DEMO_CREDENTIALS.email,
      password: DEMO_CREDENTIALS.password,
    });

    expect(response.token.split('.')).toHaveLength(3);
    expect(response.username).toBe('admin');
    expect(response.id).toBe(1);
  });

  it('surfaces the message auth-service returns for a rejected login', async () => {
    await expect(login({ email: DEMO_CREDENTIALS.email, password: 'wrong' })).rejects.toThrow(
      ApiError,
    );

    await expect(
      login({ email: DEMO_CREDENTIALS.email, password: 'wrong' }),
    ).rejects.toMatchObject({
      status: 401,
      message: 'Email veya şifre hatalı.',
    });
  });
});

describe('error handling', () => {
  it('drops the token and notifies the app when the gateway answers 401', async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);
    setAuthToken(null); // no bearer token: the handler mirrors AuthFilter and rejects

    const error = await listVenues().catch((cause: unknown) => cause);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(401);
    expect((error as ApiError).isUnauthorized).toBe(true);
    expect(getAuthToken()).toBeNull();
    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it('reports an unreachable gateway as a transport failure instead of throwing raw', async () => {
    server.use(http.get(urlOf(API_ROUTES.venues), () => HttpResponse.error()));
    setAuthToken('header.payload.signature');

    const error = await listVenues().catch((cause: unknown) => cause);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(0);
    expect((error as ApiError).message).toContain('gateway');
  });

  it('prefers the message from the backend error body', async () => {
    server.use(
      http.get(urlOf(API_ROUTES.venues), () =>
        HttpResponse.json(
          {
            timestamp: '2026-08-09T12:00:00',
            status: 500,
            error: 'Internal Server Error',
            message: 'Venue repository is unavailable',
          },
          { status: 500 },
        ),
      ),
    );
    setAuthToken('header.payload.signature');

    await expect(listVenues()).rejects.toMatchObject({
      status: 500,
      message: 'Venue repository is unavailable',
    });
  });

  it('rejects a payload that does not match the documented contract', async () => {
    server.use(http.get(urlOf(API_ROUTES.events), () => HttpResponse.json({ items: [] })));
    setAuthToken('header.payload.signature');

    await expect(listEvents(0)).rejects.toThrow(/paged envelope/);
  });
});

describe('authenticated requests', () => {
  it('sends the bearer token and returns the typed page', async () => {
    setAuthToken('header.payload.signature');

    const page = await listEvents(0);

    expect(page.content.length).toBeGreaterThan(0);
    expect(page.number).toBe(0);
    expect(page.totalElements).toBe(page.content.length);
    expect(page.content.every((event) => typeof event.id === 'string')).toBe(true);
  });
});
