import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth.ts'
import { getStoredToken, setStoredToken } from '../api/client.ts'
import type { AuthResponse, Role, UserProfile } from '../api/types.ts'

interface AuthUser {
  id: number
  name: string
  email: string
  role: Role
}

interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  loading: boolean
  isAuthenticated: boolean
  isAdmin: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

const USER_KEY = 'wallet_user'

function toAuthUser(source: AuthResponse | UserProfile): AuthUser {
  if ('userId' in source) {
    return {
      id: source.userId,
      name: source.name,
      email: source.email,
      role: source.role,
    }
  }
  return {
    id: source.id,
    name: source.name,
    email: source.email,
    role: source.role,
  }
}

function readStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

function persistSession(token: string, user: AuthUser): void {
  setStoredToken(token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

function clearSession(): void {
  setStoredToken(null)
  localStorage.removeItem(USER_KEY)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [user, setUser] = useState<AuthUser | null>(() => readStoredUser())
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false

    async function hydrate() {
      const stored = getStoredToken()
      if (!stored) {
        if (!cancelled) {
          setToken(null)
          setUser(null)
          setLoading(false)
        }
        return
      }

      try {
        const profile = await authApi.fetchMe()
        if (cancelled) return
        const nextUser = toAuthUser(profile)
        setToken(stored)
        setUser(nextUser)
        localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
      } catch {
        if (cancelled) return
        clearSession()
        setToken(null)
        setUser(null)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    void hydrate()
    return () => {
      cancelled = true
    }
  }, [])

  const applyAuth = useCallback((response: AuthResponse) => {
    const nextUser = toAuthUser(response)
    persistSession(response.accessToken, nextUser)
    setToken(response.accessToken)
    setUser(nextUser)
  }, [])

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await authApi.login(email, password)
      applyAuth(response)
    },
    [applyAuth],
  )

  const register = useCallback(
    async (name: string, email: string, password: string) => {
      const response = await authApi.register(name, email, password)
      applyAuth(response)
    },
    [applyAuth],
  )

  const logout = useCallback(() => {
    clearSession()
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      loading,
      isAuthenticated: Boolean(token && user),
      isAdmin: user?.role === 'ADMIN',
      login,
      register,
      logout,
    }),
    [user, token, loading, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
