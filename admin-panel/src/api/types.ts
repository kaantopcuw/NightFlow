/**
 * TypeScript mirrors of the NightFlow backend DTOs.
 *
 * Every interface below is a transcription of a Java class in this repository;
 * the source is named above each one so the two can be diffed by hand. Nothing
 * here is invented — fields that the backend can leave `null` are typed
 * `| null`, because none of the services enable `@JsonInclude(NON_NULL)`.
 *
 * Java `LocalDateTime` is rendered by Jackson as an ISO-8601 local date-time
 * string without a zone offset (Spring Boot defaults
 * `spring.jackson.serialization.write-dates-as-timestamps` to `false`), e.g.
 * "2026-08-09T21:00:00".
 */

/* ------------------------------------------------------------------ auth-service */

/** auth-service: `com.nightflow.authservice.dto.LoginRequest` */
export interface LoginRequest {
  email: string;
  password: string;
}

/** auth-service: `com.nightflow.authservice.dto.AuthResponse` */
export interface AuthResponse {
  token: string;
  username: string;
  id: number;
  role: string;
}

/* --------------------------------------------------------- event-catalog-service */

/** event-catalog-service: `com.nightflow.eventcatalogservice.document.EventCategory` */
export type EventCategory =
  | 'CONCERT'
  | 'THEATER'
  | 'SPORTS'
  | 'FESTIVAL'
  | 'COMEDY'
  | 'EXHIBITION';

/** event-catalog-service: `com.nightflow.eventcatalogservice.document.EventStatus` */
export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'SOLD_OUT' | 'CANCELLED';

/** event-catalog-service: `com.nightflow.eventcatalogservice.dto.EventResponse` */
export interface EventResponse {
  id: string;
  name: string;
  description: string | null;
  slug: string;

  venueId: string | null;
  venueName: string | null;
  venueCity: string | null;

  organizerId: string | null;
  organizerName: string | null;

  eventDate: string | null;
  doorsOpenAt: string | null;

  category: EventCategory | null;
  tags: string[] | null;

  posterUrl: string | null;
  galleryUrls: string[] | null;

  minPrice: number | null;
  maxPrice: number | null;

  status: EventStatus | null;
  featured: boolean | null;

  createdAt: string | null;
  updatedAt: string | null;
}

/* ------------------------------------------------------------------ venue-service */

/** venue-service: `com.nightflow.venueservice.entity.VenueType` */
export type VenueType =
  | 'STADIUM'
  | 'ARENA'
  | 'CLUB'
  | 'THEATER'
  | 'OUTDOOR'
  | 'CONCERT_HALL';

/** venue-service: `com.nightflow.venueservice.dto.VenueResponse` */
export interface VenueResponse {
  id: number;
  name: string;
  address: string | null;
  city: string | null;
  district: string | null;
  capacity: number | null;
  mapUrl: string | null;
  imageUrl: string | null;
  type: VenueType | null;
  createdAt: string | null;
}

/* ----------------------------------------------------------------- ticket-service */

/** ticket-service: `com.nightflow.ticketservice.entity.TicketStatus` */
export type TicketStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'USED' | 'CANCELLED';

/** ticket-service: `com.nightflow.ticketservice.dto.TicketResponse` */
export interface TicketResponse {
  id: number;
  ticketCode: string;
  categoryId: number | null;
  categoryName: string | null;
  eventId: string | null;
  orderId: number | null;
  userId: number | null;
  seatInfo: string | null;
  status: TicketStatus | null;
  reservedAt: string | null;
  soldAt: string | null;
  usedAt: string | null;
}

/* ------------------------------------------------------------------------ shared */

/**
 * The subset of Spring Data's `Page<T>` JSON that this panel relies on.
 *
 * `GET /api/events` returns `Page<EventResponse>`. Spring Boot 4 still defaults
 * `spring.data.web.pageable.serialization-mode` to `DIRECT`, which serialises
 * the flat `PageImpl` shape used here. `expectPage` in `client.ts` also accepts
 * the nested `PagedModel` shape produced by `VIA_DTO`, so flipping that
 * property in the backend config would not break the panel.
 */
export interface PageResult<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/**
 * Error body shared by the `@RestControllerAdvice` handlers in
 * event-catalog-service, venue-service and ticket-service. auth-service returns
 * a narrower `{ message }` record for login failures.
 */
export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  errors?: Record<string, string>;
}
