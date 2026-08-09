import { createContext } from 'react';
import type { LoginRequest } from '../api/types';
import type { Session } from './session';

export interface AuthContextValue {
  /** `null` while signed out. Restored synchronously from `sessionStorage`. */
  readonly session: Session | null;
  readonly signIn: (credentials: LoginRequest) => Promise<void>;
  readonly signOut: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
