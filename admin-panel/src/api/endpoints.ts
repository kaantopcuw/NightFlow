import { ApiError, expectArray, expectPage, request } from './client';
import type {
  AuthResponse,
  EventResponse,
  LoginRequest,
  PageResult,
  TicketResponse,
  VenueResponse,
} from './types';

/**
 * Paths as seen by the client, i.e. gateway paths. gateway-service applies
 * `StripPrefix=1`, so `/api/events` reaches event-catalog-service as `/events`.
 *
 * All four are exercised against a running stack by `gateway.live.test.ts`
 * (`npm run test:live`). That includes `venues` being plural: the gateway route
 * predicate used to read `/api/venue/**`, which made the venue list
 * unreachable. It is fixed, and the live suite pins both halves — the plural
 * path serves the list, the singular one 404s.
 */
export const API_ROUTES = {
  login: '/api/auth/login',
  events: '/api/events',
  venues: '/api/venues',
  myTickets: '/api/tickets/my-tickets',
} as const;

export const EVENTS_PAGE_SIZE = 20;

function expectAuthResponse(payload: unknown): AuthResponse {
  if (
    typeof payload !== 'object' ||
    payload === null ||
    typeof (payload as { token?: unknown }).token !== 'string' ||
    (payload as { token: string }).token.length === 0
  ) {
    throw new ApiError('The auth service did not return a usable token.', 0, payload);
  }

  const body = payload as Partial<AuthResponse> & { token: string };
  return {
    token: body.token,
    username: typeof body.username === 'string' ? body.username : '',
    id: typeof body.id === 'number' ? body.id : 0,
    role: typeof body.role === 'string' ? body.role : 'USER',
  };
}

/** `POST /api/auth/login` → auth-service `AuthController#login`. */
export async function login(credentials: LoginRequest, signal?: AbortSignal): Promise<AuthResponse> {
  const payload = await request(API_ROUTES.login, {
    method: 'POST',
    body: credentials,
    auth: false,
    signal,
  });
  return expectAuthResponse(payload);
}

/** `GET /api/events` → event-catalog-service `EventController#findAll` (paged). */
export async function listEvents(
  page: number,
  signal?: AbortSignal,
): Promise<PageResult<EventResponse>> {
  const query = new URLSearchParams({
    page: String(page),
    size: String(EVENTS_PAGE_SIZE),
  });
  const payload = await request(`${API_ROUTES.events}?${query.toString()}`, { signal });
  return expectPage<EventResponse>(payload, 'events');
}

/** `GET /api/venues` → venue-service `VenueController#findAll`. */
export async function listVenues(signal?: AbortSignal): Promise<VenueResponse[]> {
  const payload = await request(API_ROUTES.venues, { signal });
  return expectArray<VenueResponse>(payload, 'venues');
}

/**
 * `GET /api/tickets/my-tickets` → ticket-service `TicketController#getMyTickets`.
 *
 * Returns the tickets of the signed-in user. ticket-service exposes no
 * "all tickets" view intended for end users: `/tickets/event/{id}/all` is
 * guarded by the internal `SYSTEM` role.
 *
 * That guard does not currently hold — `POST /api/auth/register` copies the
 * caller's `role` string straight onto the new user, so anyone can mint a
 * `SYSTEM` token and call the internal endpoints (verified against a live
 * stack, 2026-08-09). The panel deliberately does not use that hole; fixing it
 * belongs to auth-service. See "Backend defects found while testing" in
 * README.md.
 */
export async function listMyTickets(signal?: AbortSignal): Promise<TicketResponse[]> {
  const payload = await request(API_ROUTES.myTickets, { signal });
  return expectArray<TicketResponse>(payload, 'tickets');
}
