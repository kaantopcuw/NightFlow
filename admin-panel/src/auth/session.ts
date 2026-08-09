import type { AuthResponse } from '../api/types';

/** What the panel keeps about the signed-in user, derived from `AuthResponse`. */
export interface Session {
  token: string;
  username: string;
  userId: number;
  role: string;
}

const STORAGE_KEY = 'nightflow.admin.session';

export function sessionFromAuthResponse(response: AuthResponse): Session {
  return {
    token: response.token,
    username: response.username,
    userId: response.id,
    role: response.role,
  };
}

function isSession(value: unknown): value is Session {
  if (typeof value !== 'object' || value === null) {
    return false;
  }
  const candidate = value as Record<string, unknown>;
  return (
    typeof candidate['token'] === 'string' &&
    candidate['token'].length > 0 &&
    typeof candidate['username'] === 'string' &&
    typeof candidate['userId'] === 'number' &&
    typeof candidate['role'] === 'string'
  );
}

/**
 * `sessionStorage` rather than `localStorage`: the token dies with the tab,
 * which is the closest thing to a sane lifetime for a bearer token held by a
 * single-page app.
 */
export function readStoredSession(): Session | null {
  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY);
    if (raw === null) {
      return null;
    }
    const parsed: unknown = JSON.parse(raw);
    return isSession(parsed) ? parsed : null;
  } catch {
    // Storage disabled or corrupted entry: behave like a signed-out user.
    return null;
  }
}

export function writeStoredSession(session: Session | null): void {
  try {
    if (session === null) {
      window.sessionStorage.removeItem(STORAGE_KEY);
    } else {
      window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    }
  } catch {
    // Non-fatal: the panel still works for the lifetime of the page.
  }
}
