import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

const NAV_ITEMS = [
  { to: '/events', label: 'Events' },
  { to: '/venues', label: 'Venues' },
  { to: '/tickets', label: 'My tickets' },
] as const;

export function Layout() {
  const { session, signOut } = useAuth();
  const navigate = useNavigate();

  const handleSignOut = () => {
    signOut();
    void navigate('/login', { replace: true });
  };

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-brand">
          <span className="app-brand__mark">NF</span>
          <span className="app-brand__name">NightFlow Admin</span>
        </div>

        <nav className="app-nav" aria-label="Sections">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? 'app-nav__link is-active' : 'app-nav__link')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="app-user">
          {session !== null && (
            <span className="app-user__name">
              {session.username}
              <span className="badge">{session.role}</span>
            </span>
          )}
          <button type="button" className="button button--ghost" onClick={handleSignOut}>
            Sign out
          </button>
        </div>
      </header>

      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
