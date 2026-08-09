import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { setAuthToken, setUnauthorizedHandler } from '../api/client';
import { login } from '../api/endpoints';
import type { LoginRequest } from '../api/types';
import { AuthContext, type AuthContextValue } from './AuthContext';
import {
  readStoredSession,
  sessionFromAuthResponse,
  writeStoredSession,
  type Session,
} from './session';

/**
 * Reads back a session persisted by an earlier page load.
 *
 * The API client is primed here rather than from an effect on purpose: child
 * effects run before parent effects, so a protected screen would otherwise fire
 * its very first request before the provider had installed the token.
 * `sessionStorage` is synchronous, so there is no reason to defer this at all.
 */
function restoreSession(): Session | null {
  const stored = readStoredSession();
  setAuthToken(stored?.token ?? null);
  return stored;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(restoreSession);

  const clearSession = useCallback(() => {
    setAuthToken(null);
    writeStoredSession(null);
    setSession(null);
  }, []);

  // A 401 from any endpoint invalidates the session immediately; the protected
  // routes then redirect to /login on the next render.
  useEffect(() => {
    setUnauthorizedHandler(clearSession);
    return () => {
      setUnauthorizedHandler(null);
    };
  }, [clearSession]);

  const signIn = useCallback(async (credentials: LoginRequest) => {
    const next = sessionFromAuthResponse(await login(credentials));
    setAuthToken(next.token);
    writeStoredSession(next);
    setSession(next);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ session, signIn, signOut: clearSession }),
    [session, signIn, clearSession],
  );

  return <AuthContext value={value}>{children}</AuthContext>;
}
