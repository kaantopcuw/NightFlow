import type { ApiErrorBody, PageResult } from './types';

/** Prefix for every request. Empty by default so calls stay same-origin. */
const API_BASE_URL: string = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '');

const NETWORK_ERROR_MESSAGE =
  'Could not reach the NightFlow gateway. Is gateway-service running on the configured address?';

const STATUS_MESSAGES: Readonly<Record<number, string>> = {
  401: 'Your session has expired. Please sign in again.',
  403: 'You are not allowed to perform this action.',
  404: 'The requested resource was not found.',
  500: 'The service failed while handling the request.',
  502: 'The gateway could not reach the target service.',
  503: 'The target service is currently unavailable.',
};

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface RequestOptions {
  readonly method?: HttpMethod;
  readonly body?: unknown;
  readonly signal?: AbortSignal;
  /** Send the stored bearer token. Only the login call opts out. */
  readonly auth?: boolean;
}

/**
 * Every failure the client can produce, including transport failures (status 0)
 * and contract violations, so callers never have to inspect a raw `Response`.
 */
export class ApiError extends Error {
  readonly status: number;
  readonly body: unknown;

  constructor(message: string, status: number, body: unknown = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }

  get isUnauthorized(): boolean {
    return this.status === 401;
  }
}

/* ------------------------------------------------------------- token plumbing */

let authToken: string | null = null;
let unauthorizedHandler: (() => void) | null = null;

export function setAuthToken(token: string | null): void {
  authToken = token;
}

export function getAuthToken(): string | null {
  return authToken;
}

/**
 * Registered by the auth provider so a 401 anywhere in the app drops the
 * session and sends the user back to the login screen.
 */
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

/* --------------------------------------------------------------- type guards */

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function asErrorBody(value: unknown): ApiErrorBody | null {
  return isRecord(value) ? (value as ApiErrorBody) : null;
}

function isAbortError(value: unknown): boolean {
  return value instanceof Error && value.name === 'AbortError';
}

function readNumber(value: unknown, fallback: number): number {
  return typeof value === 'number' && Number.isFinite(value) ? value : fallback;
}

/**
 * Asserts that a payload really is a JSON array before it is handed to the UI
 * as `T[]`. The cast is the one unavoidable trust boundary; validating the
 * envelope means a contract change surfaces as a readable error instead of a
 * render-time crash.
 */
export function expectArray<T>(value: unknown, resource: string): T[] {
  if (!Array.isArray(value)) {
    throw new ApiError(`Unexpected response for ${resource}: expected a JSON array.`, 0, value);
  }
  return value as T[];
}

/** Same idea for Spring Data's paged envelope. */
export function expectPage<T>(value: unknown, resource: string): PageResult<T> {
  if (!isRecord(value) || !Array.isArray(value.content)) {
    throw new ApiError(`Unexpected response for ${resource}: expected a paged envelope.`, 0, value);
  }

  const content = value.content as T[];
  // `DIRECT` serialisation puts the metadata on the root object, `VIA_DTO`
  // nests it under `page`. Accept both.
  const meta = isRecord(value.page) ? value.page : value;

  return {
    content,
    number: readNumber(meta['number'], 0),
    size: readNumber(meta['size'], content.length),
    totalElements: readNumber(meta['totalElements'], content.length),
    totalPages: readNumber(meta['totalPages'], 1),
  };
}

/* -------------------------------------------------------------------- request */

function describeFailure(payload: unknown, status: number): string {
  if (typeof payload === 'string' && payload.trim().length > 0) {
    return payload;
  }

  const body = asErrorBody(payload);
  if (body) {
    if (typeof body.message === 'string' && body.message.length > 0) {
      return body.message;
    }
    if (isRecord(body.errors)) {
      const fields = Object.entries(body.errors)
        .filter((entry): entry is [string, string] => typeof entry[1] === 'string')
        .map(([field, message]) => `${field}: ${message}`);
      if (fields.length > 0) {
        return fields.join(' · ');
      }
    }
    if (typeof body.error === 'string' && body.error.length > 0) {
      return body.error;
    }
  }

  return STATUS_MESSAGES[status] ?? `Request failed with status ${status}.`;
}

async function readPayload(response: Response): Promise<unknown> {
  if (response.status === 204) {
    return null;
  }
  const text = await response.text();
  if (text.length === 0) {
    return null;
  }
  try {
    return JSON.parse(text) as unknown;
  } catch {
    // Some gateway/filter failures answer with plain text ("Direct service
    // access not allowed"), which is still worth showing to the operator.
    return text;
  }
}

/**
 * The single fetch wrapper. Returns `unknown` on purpose: callers in
 * `endpoints.ts` narrow the payload through the guards above, so no `any`
 * leaks into the application.
 */
export async function request(path: string, options: RequestOptions = {}): Promise<unknown> {
  const { method = 'GET', body, signal, auth = true } = options;

  const headers: Record<string, string> = { Accept: 'application/json' };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  if (auth && authToken !== null) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    });
  } catch (cause) {
    if (isAbortError(cause)) {
      throw cause;
    }
    throw new ApiError(NETWORK_ERROR_MESSAGE, 0, null);
  }

  const payload = await readPayload(response);

  if (!response.ok) {
    if (response.status === 401) {
      setAuthToken(null);
      unauthorizedHandler?.();
    }
    throw new ApiError(describeFailure(payload, response.status), response.status, payload);
  }

  return payload;
}

/** Turns anything thrown by the client into a message safe to render. */
export function toErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (error instanceof Error && error.message.length > 0) {
    return error.message;
  }
  return 'Something went wrong while loading data.';
}
