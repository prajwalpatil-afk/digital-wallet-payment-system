import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client.ts'
import { ErrorAlert } from '../components/Alert.tsx'
import { useAuth } from '../context/AuthContext.tsx'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await register(name.trim(), email.trim(), password)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(getApiErrorMessage(err, 'Registration failed'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-panel">
        <p className="brand">PayVault</p>
        <h1>Create account</h1>
        <p className="muted">Register to get a wallet with balance ₹0.</p>

        <form className="stack-form" onSubmit={handleSubmit}>
          <ErrorAlert message={error} />
          <label>
            Name
            <input
              type="text"
              autoComplete="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              maxLength={120}
            />
          </label>
          <label>
            Email
            <input
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
            />
          </label>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create account'}
          </button>
        </form>

        <p className="auth-footer">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
