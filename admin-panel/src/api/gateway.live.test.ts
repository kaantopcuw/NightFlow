/**
 * Live integration suite — runs the panel's own API client against a running
 * NightFlow backend. Excluded from `npm test`; run it with `npm run test:live`
 * while the docker-compose stack is up (see README.md).
 *
 * Nothing here is mocked: `request()`, the guards in `client.ts` and the
 * endpoint wrappers in `endpoints.ts` are the exact modules the browser loads.
 * The point is to prove that the contract `types.ts` transcribes from the Java
 * DTOs is the contract the services actually serve.
 */
import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import {
  readStoredSession,
  sessionFromAuthResponse,
  writeStoredSession,
} from '../auth/session';
import { formatDateTime } from '../lib/format';
import {
  ApiError,
  getAuthToken,
  request,
  setAuthToken,
  setUnauthorizedHandler,
} from './client';
import { API_ROUTES, listEvents, listMyTickets, listVenues, login } from './endpoints';
import type { AuthResponse, EventResponse, TicketResponse, VenueResponse } from './types';

const CREDENTIALS = {
  email: import.meta.env.VITE_LIVE_EMAIL ?? 'reviewbot@example.com',
  password: import.meta.env.VITE_LIVE_PASSWORD ?? 'test1234',
};

/**
 * Well-formed enough to survive `AuthFilter`'s cheap format check (three
 * non-empty dot-separated parts, 20–2048 characters), so the request reaches
 * auth-service's `/auth/validate` and is rejected there instead.
 */
const SYNTACTICALLY_VALID_BUT_FAKE_TOKEN = 'aaaaaaaaaa.bbbbbbbbbb.cccccccccc';

/* ------------------------------------------------------- runtime DTO checking */

type FieldKind = 'string' | 'number' | 'boolean' | 'string[]';

interface FieldSpec {
  readonly kind: FieldKind;
  /** The Java DTOs are plain POJOs with no `@JsonInclude`, so most fields
   *  serialise as `null` rather than disappearing. `types.ts` mirrors that. */
  readonly nullable: boolean;
}

function required(kind: FieldKind): FieldSpec {
  return { kind, nullable: false };
}

function nullable(kind: FieldKind): FieldSpec {
  return { kind, nullable: true };
}

function kindOf(value: unknown): string {
  if (value === null) {
    return 'null';
  }
  if (Array.isArray(value)) {
    return value.every((item) => typeof item === 'string') ? 'string[]' : 'unknown[]';
  }
  return typeof value;
}

/**
 * Asserts that a live payload matches the interface in `types.ts` exactly:
 * every declared field present with a declared kind, and no field the panel
 * does not model. The second half is the one that catches silent DTO drift.
 */
function expectDto(
  value: unknown,
  spec: Readonly<Record<string, FieldSpec>>,
  label: string,
): void {
  expect(value, `${label} should be a JSON object`).toBeTypeOf('object');
  expect(value, `${label} should not be null`).not.toBeNull();
  const record = value as Record<string, unknown>;

  for (const [name, { kind, nullable: isNullable }] of Object.entries(spec)) {
    expect(Object.hasOwn(record, name), `${label}.${name} is absent from the live payload`).toBe(
      true,
    );
    const allowed = isNullable ? [kind, 'null'] : [kind];
    expect(
      allowed,
      `${label}.${name} arrived as ${kindOf(record[name])}, expected ${allowed.join(' | ')}`,
    ).toContain(kindOf(record[name]));
  }

  const unmodelled = Object.keys(record).filter((name) => !Object.hasOwn(spec, name));
  expect(unmodelled, `${label} carries fields src/api/types.ts does not model`).toEqual([]);
}

const AUTH_RESPONSE_SPEC: Readonly<Record<keyof AuthResponse, FieldSpec>> = {
  token: required('string'),
  username: required('string'),
  id: required('number'),
  role: required('string'),
};

const VENUE_SPEC: Readonly<Record<keyof VenueResponse, FieldSpec>> = {
  id: required('number'),
  name: required('string'),
  address: nullable('string'),
  city: nullable('string'),
  district: nullable('string'),
  capacity: nullable('number'),
  mapUrl: nullable('string'),
  imageUrl: nullable('string'),
  type: nullable('string'),
  createdAt: nullable('string'),
};

const EVENT_SPEC: Readonly<Record<keyof EventResponse, FieldSpec>> = {
  id: required('string'),
  name: required('string'),
  description: nullable('string'),
  slug: required('string'),
  venueId: nullable('string'),
  venueName: nullable('string'),
  venueCity: nullable('string'),
  organizerId: nullable('string'),
  organizerName: nullable('string'),
  eventDate: nullable('string'),
  doorsOpenAt: nullable('string'),
  category: nullable('string'),
  tags: nullable('string[]'),
  posterUrl: nullable('string'),
  galleryUrls: nullable('string[]'),
  minPrice: nullable('number'),
  maxPrice: nullable('number'),
  status: nullable('string'),
  featured: nullable('boolean'),
  createdAt: nullable('string'),
  updatedAt: nullable('string'),
};

const TICKET_SPEC: Readonly<Record<keyof TicketResponse, FieldSpec>> = {
  id: required('number'),
  ticketCode: required('string'),
  categoryId: nullable('number'),
  categoryName: nullable('string'),
  eventId: nullable('string'),
  orderId: nullable('number'),
  userId: nullable('number'),
  seatInfo: nullable('string'),
  status: nullable('string'),
  reservedAt: nullable('string'),
  soldAt: nullable('string'),
  usedAt: nullable('string'),
};

/** The enum members `types.ts` declares, checked against what the live rows use. */
const VENUE_TYPES = ['STADIUM', 'ARENA', 'CLUB', 'THEATER', 'OUTDOOR', 'CONCERT_HALL'];
const EVENT_CATEGORIES = ['CONCERT', 'THEATER', 'SPORTS', 'FESTIVAL', 'COMEDY', 'EXHIBITION'];
const EVENT_STATUSES = ['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED'];
const TICKET_STATUSES = ['AVAILABLE', 'RESERVED', 'SOLD', 'USED', 'CANCELLED'];

/* ----------------------------------------------------------------- the suite */

let auth: AuthResponse;

beforeAll(async () => {
  auth = await login(CREDENTIALS);
  setAuthToken(auth.token);

  // venue-service accepts a POST from any authenticated user, so the suite can
  // guarantee itself at least one row on a freshly created database instead of
  // silently passing over an empty list.
  const existing = await listVenues();
  if (existing.length === 0) {
    await request(API_ROUTES.venues, {
      method: 'POST',
      body: {
        name: 'Live Test Venue',
        city: 'Istanbul',
        district: 'Kadikoy',
        capacity: 750,
        type: 'CLUB',
      },
    });
  }
});

afterEach(() => {
  // Individual tests deliberately clear or corrupt the token; put it back.
  setAuthToken(auth.token);
  setUnauthorizedHandler(null);
  window.sessionStorage.clear();
});

describe('sign in against auth-service', () => {
  it('returns an AuthResponse whose live JSON matches src/api/types.ts', async () => {
    // `login()` normalises the payload, so the raw body is checked as well —
    // that is where an added or renamed auth-service field would show up.
    const raw = await request(API_ROUTES.login, {
      method: 'POST',
      body: CREDENTIALS,
      auth: false,
    });

    expectDto(raw, AUTH_RESPONSE_SPEC, 'AuthResponse');
    expect(auth.token.split('.')).toHaveLength(3);
    expect(auth.username.length).toBeGreaterThan(0);
    expect(auth.id).toBeGreaterThan(0);
  });

  it('rejects a wrong password with the message auth-service produces', async () => {
    const error = await login({ ...CREDENTIALS, password: 'definitely-not-it' }).catch(
      (cause: unknown) => cause,
    );

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(401);
    // auth-service answers `{"message": "..."}`; the client must surface it
    // instead of the generic 401 text.
    expect((error as ApiError).message).toBe('Email veya şifre hatalı.');
  });

  it('round-trips the live token through sessionStorage', () => {
    const session = sessionFromAuthResponse(auth);
    writeStoredSession(session);

    expect(readStoredSession()).toEqual(session);
    expect(session.token).toBe(auth.token);
    expect(session.userId).toBe(auth.id);
  });
});

describe('GET /api/venues against venue-service', () => {
  it('returns a JSON array that the panel renders as VenueResponse[]', async () => {
    const venues = await listVenues();

    expect(Array.isArray(venues)).toBe(true);
    expect(venues.length).toBeGreaterThan(0);
    for (const venue of venues) {
      expectDto(venue, VENUE_SPEC, 'VenueResponse');
      if (venue.type !== null) {
        expect(VENUE_TYPES).toContain(venue.type);
      }
      if (venue.createdAt !== null) {
        // Jackson writes microsecond precision here; `new Date()` must still
        // parse it, or the venue list would print raw timestamps.
        expect(formatDateTime(venue.createdAt)).not.toBe(venue.createdAt);
      }
    }
  });
});

describe('GET /api/events against event-catalog-service', () => {
  it('returns a paged envelope that expectPage() accepts', async () => {
    const page = await listEvents(0);

    expect(Array.isArray(page.content)).toBe(true);
    expect(page.number).toBe(0);
    expect(page.size).toBeGreaterThan(0);
    expect(page.totalElements).toBeGreaterThanOrEqual(page.content.length);
    expect(page.totalPages).toBeGreaterThanOrEqual(0);
  });

  it('serves events whose live JSON matches src/api/types.ts', async () => {
    const page = await listEvents(0);

    // `EventService#findAll` only returns PUBLISHED events, so an empty page
    // means the catalogue was never seeded — see README, "Seeding data".
    expect(page.content.length).toBeGreaterThan(0);

    for (const event of page.content) {
      expectDto(event, EVENT_SPEC, 'EventResponse');
      if (event.category !== null) {
        expect(EVENT_CATEGORIES).toContain(event.category);
      }
      if (event.status !== null) {
        expect(EVENT_STATUSES).toContain(event.status);
      }
      if (event.eventDate !== null) {
        expect(formatDateTime(event.eventDate)).not.toBe(event.eventDate);
      }
    }
  });

  it('reports the last page as empty rather than failing', async () => {
    const first = await listEvents(0);
    const beyond = await listEvents(first.totalPages + 5);

    expect(beyond.content).toHaveLength(0);
    expect(beyond.totalElements).toBe(first.totalElements);
  });
});

describe('GET /api/tickets/my-tickets against ticket-service', () => {
  it('returns the signed-in user’s tickets as a JSON array', async () => {
    const tickets = await listMyTickets();

    expect(Array.isArray(tickets)).toBe(true);
    for (const ticket of tickets) {
      expectDto(ticket, TICKET_SPEC, 'TicketResponse');
      if (ticket.status !== null) {
        expect(TICKET_STATUSES).toContain(ticket.status);
      }
    }
  });
});

describe('the 401 path through gateway-service', () => {
  it('turns a bodiless gateway 401 into the session-expired message', async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);
    setAuthToken(null);

    const error = await listVenues().catch((cause: unknown) => cause);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(401);
    expect((error as ApiError).isUnauthorized).toBe(true);
    // AuthFilter answers `401` with `content-length: 0`, so there is no backend
    // message to show and the client must fall back to its own wording.
    expect((error as ApiError).message).toBe('Your session has expired. Please sign in again.');
    expect(getAuthToken()).toBeNull();
    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it('rejects a token that auth-service cannot validate', async () => {
    const onUnauthorized = vi.fn();
    setUnauthorizedHandler(onUnauthorized);
    setAuthToken(SYNTACTICALLY_VALID_BUT_FAKE_TOKEN);

    const error = await listEvents(0).catch((cause: unknown) => cause);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(401);
    expect(getAuthToken()).toBeNull();
    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

});

describe('gateway routing', () => {
  it('serves the venue list on the plural path the panel targets', async () => {
    await expect(request(API_ROUTES.venues)).resolves.toBeInstanceOf(Array);
  });

  it('has no route for the singular /api/venue path', async () => {
    const error = await request('/api/venue').catch((cause: unknown) => cause);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(404);
  });
});
