import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './useAuth';

/**
 * Layout route that admits only authenticated users. Nothing behind it is ever
 * mounted without a session, so no protected screen can fire an API request as
 * an anonymous user.
 */
export function ProtectedRoute() {
  const { session } = useAuth();
  const location = useLocation();

  if (session === null) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
