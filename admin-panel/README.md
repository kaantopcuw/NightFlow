# NightFlow Admin Panel

A small, strictly typed React + TypeScript operator panel for the NightFlow
platform. It authenticates against `auth-service`, keeps the returned JWT, and
reads three lists back through `gateway-service`.

The scope is deliberately narrow: a correct, fully working slice rather than a
broad half-finished console. Everything it calls exists in the Java services in
this repository — no endpoint or field was invented.

| | |
|---|---|
| Stack | Vite 8, React 19, TypeScript 6 (`strict`), React Router 7 |
| Tests | Vitest 4, Testing Library, MSW 2 — plus a live suite against a real gateway |
| Entry point | `gateway-service` on `http://localhost:8080` |

The contract below is not just transcribed from the Java sources: it was
exercised against a running stack on 2026-08-09 and is pinned by
`npm run test:live`.

## Screens

| Route | What it shows | Endpoint |
|---|---|---|
| `/login` | Email + password sign-in | `POST /api/auth/login` |
| `/events` | Paged event catalogue with prev/next | `GET /api/events?page&size` |
| `/venues` | All registered venues | `GET /api/venues` |
| `/tickets` | Tickets owned by the signed-in user | `GET /api/tickets/my-tickets` |

Every list has explicit loading, error (with retry) and empty states.

`/tickets` shows *the signed-in user's* tickets. `ticket-service` exposes no
"all tickets" view meant for end users: `GET /tickets/event/{id}/all` is guarded
by the internal `SYSTEM` role. That guard used to be defeatable through
`POST /api/auth/register` — see "Backend defects found while testing" — and is
not any more; the panel does not use the endpoint either way.

`/events` lists only `PUBLISHED` events, because that is all
`EventService#findAll` returns; a `DRAFT` event is invisible to the panel by
design of the backend, not by omission here.

## Backend contract

Paths are gateway paths. `gateway-service` applies `StripPrefix=1`, so
`/api/events` arrives at `event-catalog-service` as `/events`.

| Gateway path | Service | Controller | Response DTO |
|---|---|---|---|
| `POST /api/auth/login` | auth-service | `AuthController#login` | `AuthResponse { token, username, id, role }` |
| `GET /api/events` | event-catalog-service | `EventController#findAll` | `Page<EventResponse>` |
| `GET /api/venues` | venue-service | `VenueController#findAll` | `List<VenueResponse>` |
| `GET /api/tickets/my-tickets` | ticket-service | `TicketController#getMyTickets` | `List<TicketResponse>` |

`src/api/types.ts` transcribes those DTOs field by field, including the enums
(`EventCategory`, `EventStatus`, `VenueType`, `TicketStatus`) and the shared
`@RestControllerAdvice` error body (`{ timestamp, status, error, message }`).
Java `LocalDateTime` values arrive as zone-less ISO-8601 strings such as
`2026-09-12T21:00:00`, and are rendered as wall-clock time rather than shifted
into the viewer's time zone.

`GET /api/events` returns Spring Data's `Page`. Spring Boot 4 still defaults
`spring.data.web.pageable.serialization-mode` to `DIRECT`, i.e. the flat
`PageImpl` JSON; `expectPage()` also accepts the nested `PagedModel` shape that
`VIA_DTO` produces, so flipping that property would not break the panel.

### Authentication

1. `POST /api/auth/login` returns a JWT.
2. The token is stored in `sessionStorage` (it dies with the tab) and sent as
   `Authorization: Bearer <token>` on every later request.
3. `gateway-service`'s `AuthFilter` validates it against `auth-service` and
   injects `X-User-Id` / `X-User-Role` for the downstream service.
4. Any `401` clears the session and drops the user back on `/login`.

## Running it

### Against a live backend

```bash
npm install
cp .env.example .env.local     # optional; defaults already point at :8080
npm run dev                    # http://localhost:5173
```

The dev server proxies `/api/**` to `VITE_GATEWAY_URL` (default
`http://localhost:8080`). This is not cosmetic: `gateway-service` sends no CORS
headers, so the browser could not call it cross-origin. Serve the production
build from behind the same kind of proxy.

Bring the backend up from the repository root with `./manage.sh` or
`docker compose up -d` (Kafka, Redis, PostgreSQL, MongoDB and eleven services
are required). Once it is up, `npm run test:live` checks the whole path without
opening a browser — see "Live integration tests" below.

### Without a backend

```bash
VITE_ENABLE_MOCKS=true npm run dev
```

MSW intercepts the same paths in the browser and answers with fixtures typed as
the real DTOs (`src/mocks/fixtures.ts`), so the mocks cannot drift from the
contract without breaking the type check. Sign in with:

```
admin@nightflow.dev / nightflow
```

Like the real gateway, the mock rejects any request without a bearer token. The
MSW bundle is loaded through a dynamic import and is dropped entirely from a
build where `VITE_ENABLE_MOCKS` is not `true`.

### Scripts

| Command | What it does |
|---|---|
| `npm run dev` | Dev server on :5173 with the `/api` proxy |
| `npm run build` | `tsc --noEmit` then `vite build` |
| `npm run lint` | ESLint (flat config, typescript-eslint, react-hooks) |
| `npm test` | Vitest, single run — MSW-mocked, no backend needed |
| `npm run test:live` | Vitest against a **running** backend (see below) |
| `npm run preview` | Serve the production build |

### Configuration

See `.env.example`. No secrets belong in it: the only credential the panel ever
holds is a JWT the user obtained by signing in.

## Structure

```
src/
  api/
    types.ts        TypeScript mirrors of the Java DTOs and enums
    client.ts       the single fetch wrapper: ApiError, bearer token, 401 hook,
                    response guards
    endpoints.ts    one typed function per endpoint, plus the gateway paths
  auth/
    session.ts      sessionStorage persistence + validation
    AuthContext.ts  context type and object
    AuthProvider.tsx  sign in / sign out / restore
    ProtectedRoute.tsx  layout route that admits only authenticated users
  hooks/useApiResource.ts   loading | error | success as a discriminated union
  components/     AsyncBoundary (state renderer), Layout (nav + sign out)
  pages/          LoginPage, EventsPage, VenuesPage, TicketsPage
  mocks/          typed fixtures + MSW handlers (browser and tests share them)
  test/           MSW node server and Vitest setup
  api/gateway.live.test.ts   contract check against a running backend
  panel.live.test.tsx        screen rendering against a running backend
```

`*.live.test.*` files are matched by `vite.config.ts` only under
`--mode live`, and excluded from the default Vitest run.

### Type-safety notes

- `any` is banned by ESLint. `request()` returns `unknown`; every payload passes
  through a guard (`expectArray`, `expectPage`, `expectAuthResponse`) before it
  is treated as a DTO, so a contract change surfaces as a readable error instead
  of a crash mid-render.
- Those guards validate the *envelope*, not every field. Deep per-field
  validation would need a schema library and is out of scope.
- `tsconfig.json` enables `strict`, plus `noUncheckedIndexedAccess`,
  `noUnusedLocals`, `noUnusedParameters`, `noImplicitOverride`,
  `noFallthroughCasesInSwitch` and `verbatimModuleSyntax`.

## Tests

`npm test` runs 12 tests over the areas most likely to break silently:

- **API client** (`src/api/client.test.ts`) — successful login; auth-service's
  rejection message surfacing verbatim; a `401` clearing the token and notifying
  the app; an unreachable gateway reported as a transport failure rather than a
  raw `TypeError`; the backend error body preferred over a generic message; a
  payload that violates the paged contract rejected.
- **Protected routing** (`src/routing.test.tsx`) — an anonymous visit to
  `/events` lands on the login screen and issues no API call; a stored session is
  restored and renders the protected screen; the full sign-in flow; a failed
  sign-in showing the error and staying put; signing out returning to `/login`.

MSW runs with `onUnhandledRequest: 'error'`, so a screen that fires a request it
should not have made fails the suite.

### Live integration tests

`npm run test:live` runs 16 tests through the panel's own `api/client.ts` and
`api/endpoints.ts` against a **real gateway**, with MSW not installed at all.
It is deliberately kept out of `npm test`, which must stay runnable offline.

```bash
docker compose up -d                    # from the repository root
npm run test:live                       # expects the gateway on :8080
VITE_GATEWAY_URL=http://host:8080 npm run test:live   # or point it elsewhere
```

| File | What it proves |
|---|---|
| `src/api/gateway.live.test.ts` | Sign-in returns a real JWT; the token survives a `sessionStorage` round trip; `/api/venues`, `/api/events` and `/api/tickets/my-tickets` answer with payloads that match `src/api/types.ts` **field for field**, with no unmodelled fields; enum values are members of the unions declared there; `Jackson`'s `LocalDateTime` strings parse; a bodiless gateway `401` becomes the session-expired message and clears the token; a token auth-service rejects also yields `401`; `/api/venue` (singular) is a `404` |
| `src/panel.live.test.tsx` | The real route tree signs in through the login form and renders live rows on `/events` and `/venues`, renders `/tickets` without an error state, and still bounces an anonymous visitor to `/login` |

The `/tickets` assertions accept either rendered rows or the empty state, so the
suite passed both before and after the backend defects below were fixed. Now
that a ticket can actually be sold, the run exercises the row path and checks a
real `TicketResponse` against `types.ts` field for field. (One code comment in
`src/panel.live.test.tsx` still describes the old empty-only outcome; it is
stale, not wrong about the assertion.)

Two things make the suite trustworthy rather than decorative:

- Point it at a dead port and it fails with the client's own transport error
  (`VITE_GATEWAY_URL=http://127.0.0.1:9 npm run test:live`) — it cannot pass
  without a backend.
- Break one field in the DTO specs and the run fails with, for example,
  `VenueResponse.capacity arrived as number, expected string | null`. Both
  halves of the drift check were confirmed to fail on purpose before being
  trusted.

#### It needs data

`EventService#findAll` only returns `PUBLISHED` events, so an empty database
makes the events assertions fail. The suite creates a venue for itself if the
venue list is empty, but it cannot create events: `POST /api/events` requires
`ROLE_ORGANIZER`, and a newly created event is `DRAFT` until published.

```bash
# once, against an empty stack — an organizer, an event, then publish it
curl -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"organizerbot","email":"organizerbot@example.com","password":"test1234","role":"ORGANIZER"}'
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"organizerbot@example.com","password":"test1234"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
ID=$(curl -s -X POST localhost:8080/api/events -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Neon Nights Vol. 1","slug":"neon-nights-vol-1","venueId":"1","eventDate":"2026-11-14T21:00:00","category":"CONCERT"}' \
  | sed 's/.*"id":"\([^"]*\)".*/\1/')
curl -X PATCH "localhost:8080/api/events/$ID/status?status=PUBLISHED" -H "Authorization: Bearer $TOKEN"
```

`VITE_LIVE_EMAIL` / `VITE_LIVE_PASSWORD` choose the account the suite signs in
with; they default to a throwaway test user, and no real credential belongs in
them.

## Backend defects found while testing

Found while exercising the panel against a live stack on 2026-08-09. All three
were backend problems; none of them was worked around in the panel. All three
are now **fixed and re-verified against the same live stack** — the fixes are in
the Java services, the panel is unchanged.

1. **`POST /api/auth/register` granted any role the caller asked for.**
   `AuthenticationService#registerUser` copied `request.getRole()` onto the new
   user unchecked, so registering with `"role": "SYSTEM"` produced a token that
   `HeaderAuthFilter` turned into `ROLE_SYSTEM`, and
   `GET /api/tickets/event/{id}/all` — documented as internal-only — answered
   `200` for it.
   *Fixed:* the requested role is now matched against an allow-list of
   self-service roles (`USER`, `ORGANIZER`); anything else is rejected with
   `400`. `"SYSTEM"`, `"system"`, `"ROLE_SYSTEM"` and `"ADMIN"` all return `400`,
   `"ORGANIZER"` still succeeds, and the internal endpoint now answers `403`.
2. **Four Feign clients hard-coded `http://localhost:<port>`**
   (`ticket-service` → event-catalog, `shopping-cart-service`, `order-service`
   and `checkin-service` → ticket-service). Inside Docker each container has its
   own loopback, so these calls failed: creating a ticket category returned
   `500 Connection refused executing GET http://localhost:8092/events/…`.
   *Fixed:* all four now carry the Eureka service id as `@FeignClient(name = …)`
   with no `url`, which is what makes Spring Cloud resolve them as
   `lb://<service-id>`. (A literal `url = "lb://…"` does **not** work: in
   spring-cloud-openfeign a non-empty `url` bypasses load balancing entirely.)
3. **A ticket could therefore never be sold through the public API.**
   *Fixed as a consequence of 2:* ticket category → cart → order →
   `confirm-sale` now completes end to end, and `GET /api/tickets/my-tickets`
   returns `SOLD` tickets.

## Not verified

- **Browser rendering.** The screens are rendered in jsdom against live data,
  and the Vite dev proxy was confirmed to forward `/api/**` to the gateway with
  the bearer token intact, but no real browser session was driven.
- **Production hosting.** The dev-server proxy stands in for whatever fronts the
  built assets; `gateway-service` still sends no CORS headers, so a same-origin
  proxy remains mandatory in production.
