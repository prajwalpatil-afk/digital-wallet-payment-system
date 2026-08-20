import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.tsx'

export function ProtectedRoute() {
  const { isAuthenticated, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="page-center">
        <p className="muted">Loading…</p>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}

export function AdminRoute() {
  const { isAuthenticated, isAdmin, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="page-center">
        <p className="muted">Loading…</p>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}

export function GuestRoute() {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return (
      <div className="page-center">
        <p className="muted">Loading…</p>
      </div>
    )
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}
