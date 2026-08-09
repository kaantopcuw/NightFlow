import { useState, type FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { toErrorMessage } from '../api/client';
import { useAuth } from '../auth/useAuth';

/**
 * `ProtectedRoute` stores the blocked path in the navigation state. It is
 * `any` as far as react-router's types go, so it is narrowed before use.
 */
function redirectTarget(state: unknown): string {
  if (typeof state === 'object' && state !== null) {
    const from = (state as { from?: unknown }).from;
    if (typeof from === 'string' && from.startsWith('/') && from !== '/login') {
      return from;
    }
  }
  return '/events';
}

export function LoginPage() {
  const { session, signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const submit = async () => {
      setError(null);
      setIsSubmitting(true);
      try {
        await signIn({ email, password });
        void navigate(redirectTarget(location.state), { replace: true });
      } catch (cause: unknown) {
        setError(toErrorMessage(cause));
        setIsSubmitting(false);
      }
    };

    void submit();
  };

  if (session !== null) {
    return <Navigate to="/events" replace />;
  }

  return (
    <div className="login">
      <form className="login__card" onSubmit={handleSubmit} noValidate>
        <div className="login__brand">
          <span className="app-brand__mark">NF</span>
          <h1>NightFlow Admin</h1>
        </div>
        <p className="login__hint">
          Sign in with a NightFlow account. Credentials are verified by
          auth-service through the gateway.
        </p>

        <label className="field" htmlFor="email">
          <span className="field__label">Email</span>
          <input
            id="email"
            name="email"
            type="email"
            className="field__input"
            autoComplete="username"
            required
            value={email}
            onChange={(event) => {
              setEmail(event.target.value);
            }}
          />
        </label>

        <label className="field" htmlFor="password">
          <span className="field__label">Password</span>
          <input
            id="password"
            name="password"
            type="password"
            className="field__input"
            autoComplete="current-password"
            required
            value={password}
            onChange={(event) => {
              setPassword(event.target.value);
            }}
          />
        </label>

        {error !== null && (
          <p className="state state--error" role="alert">
            {error}
          </p>
        )}

        <button type="submit" className="button button--primary" disabled={isSubmitting}>
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </div>
  );
}
