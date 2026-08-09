import { HttpResponse, http, type HttpHandler } from 'msw';
import { API_ROUTES } from '../api/endpoints';
import type { EventResponse } from '../api/types';
import {
  DEMO_AUTH_RESPONSE,
  DEMO_CREDENTIALS,
  DEMO_EVENTS,
  DEMO_TICKETS,
  DEMO_VENUES,
} from './fixtures';

/**
 * A stand-in for gateway-service.
 *
 * The handlers reproduce the contract the panel actually depends on: the JSON
 * shapes come from the typed fixtures, the paged envelope matches Spring Data's
 * `DIRECT` serialisation, and — like the real `AuthFilter` — every route except
 * login answers 401 when the `Authorization` header is missing.
 */

const BASE = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '');

function url(path: string): string {
  return `${BASE}${path}`;
}

function unauthorized() {
  return HttpResponse.json(
    {
      timestamp: new Date().toISOString(),
      status: 401,
      error: 'Unauthorized',
      message: 'Missing or invalid bearer token.',
    },
    { status: 401 },
  );
}

function isAuthorized(request: Request): boolean {
  const header = request.headers.get('Authorization');
  return header !== null && header.startsWith('Bearer ') && header.length > 'Bearer '.length;
}

function readCredentials(body: unknown): { email: string; password: string } | null {
  if (typeof body !== 'object' || body === null) {
    return null;
  }
  const candidate = body as Record<string, unknown>;
  const email = candidate['email'];
  const password = candidate['password'];
  if (typeof email !== 'string' || typeof password !== 'string') {
    return null;
  }
  return { email, password };
}

function toNonNegativeInt(value: string | null, fallback: number): number {
  if (value === null) {
    return fallback;
  }
  const parsed = Number.parseInt(value, 10);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : fallback;
}

/** Mirrors Spring Data's flat `PageImpl` JSON for `GET /api/events`. */
function pageOf(items: EventResponse[], pageNumber: number, size: number) {
  const safeSize = size > 0 ? size : 20;
  const start = pageNumber * safeSize;
  const content = items.slice(start, start + safeSize);
  const totalPages = Math.max(1, Math.ceil(items.length / safeSize));

  return {
    content,
    pageable: { pageNumber, pageSize: safeSize, offset: start },
    totalElements: items.length,
    totalPages,
    last: pageNumber + 1 >= totalPages,
    first: pageNumber === 0,
    size: safeSize,
    number: pageNumber,
    numberOfElements: content.length,
    empty: content.length === 0,
  };
}

export const handlers: HttpHandler[] = [
  http.post(url(API_ROUTES.login), async ({ request }) => {
    const body: unknown = await request.json();
    const credentials = readCredentials(body);

    if (
      credentials === null ||
      credentials.email !== DEMO_CREDENTIALS.email ||
      credentials.password !== DEMO_CREDENTIALS.password
    ) {
      // auth-service returns exactly this record for a failed login.
      return HttpResponse.json({ message: 'Email veya şifre hatalı.' }, { status: 401 });
    }

    return HttpResponse.json(DEMO_AUTH_RESPONSE, { status: 200 });
  }),

  http.get(url(API_ROUTES.events), ({ request }) => {
    if (!isAuthorized(request)) {
      return unauthorized();
    }
    const query = new URL(request.url).searchParams;
    return HttpResponse.json(
      pageOf(DEMO_EVENTS, toNonNegativeInt(query.get('page'), 0), toNonNegativeInt(query.get('size'), 20)),
    );
  }),

  http.get(url(API_ROUTES.venues), ({ request }) => {
    return isAuthorized(request) ? HttpResponse.json(DEMO_VENUES) : unauthorized();
  }),

  http.get(url(API_ROUTES.myTickets), ({ request }) => {
    return isAuthorized(request) ? HttpResponse.json(DEMO_TICKETS) : unauthorized();
  }),
];
