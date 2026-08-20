import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.tsx'

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/wallet', label: 'Wallet' },
  { to: '/add-money', label: 'Add money' },
  { to: '/withdraw', label: 'Withdraw' },
  { to: '/transfer', label: 'Transfer' },
  { to: '/transactions', label: 'Transactions' },
]

export function AppLayout() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand-block">
          <p className="brand">PayVault</p>
          <p className="brand-sub">Digital wallet</p>
        </div>
        <nav className="app-nav" aria-label="Main">
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              {item.label}
            </NavLink>
          ))}
          {isAdmin && (
            <NavLink to="/admin" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              Admin
            </NavLink>
          )}
        </nav>
        <div className="header-user">
          <div>
            <p className="user-name">{user?.name}</p>
            <p className="user-email">{user?.email}</p>
          </div>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
