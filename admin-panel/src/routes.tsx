import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { Layout } from './components/Layout';
import { EventsPage } from './pages/EventsPage';
import { LoginPage } from './pages/LoginPage';
import { TicketsPage } from './pages/TicketsPage';
import { VenuesPage } from './pages/VenuesPage';

/**
 * Kept separate from `<App>` so tests can mount the whole route tree inside a
 * `MemoryRouter` instead of the browser history.
 */
export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/events" element={<EventsPage />} />
          <Route path="/venues" element={<VenuesPage />} />
          <Route path="/tickets" element={<TicketsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/events" replace />} />
    </Routes>
  );
}
