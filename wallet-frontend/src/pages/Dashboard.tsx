import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client.ts'
import type { DashboardResponse } from '../api/types.ts'
import * as walletApi from '../api/wallet.ts'
import { ErrorAlert } from '../components/Alert.tsx'
import { TransactionList } from '../components/TransactionList.tsx'
import { formatMoney } from '../lib/format.ts'
import { useAuth } from '../context/AuthContext.tsx'

export function DashboardPage() {
  const { user } = useAuth()
  const [data, setData] = useState<DashboardResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const dashboard = await walletApi.getDashboard()
        if (!cancelled) setData(dashboard)
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load dashboard'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void load()
    return () => {
      cancelled = true
    }
  }, [])

  if (loading) return <p className="muted">Loading dashboard…</p>
  if (error) return <ErrorAlert message={error} />
  if (!data) return null

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Hi, {user?.name.split(' ')[0]}</h1>
          <p className="muted">Your wallet at a glance.</p>
        </div>
        <div className="action-row">
          <Link className="btn btn-primary" to="/add-money">
            Add money
          </Link>
          <Link className="btn btn-secondary" to="/transfer">
            Transfer
          </Link>
        </div>
      </header>

      <section className="stats-grid">
        <article className="stat-card balance-card">
          <p className="stat-label">Available balance</p>
          <p className="stat-value">{formatMoney(data.balance)}</p>
        </article>
        <article className="stat-card">
          <p className="stat-label">Total deposited</p>
          <p className="stat-value">{formatMoney(data.totalDeposited)}</p>
        </article>
        <article className="stat-card">
          <p className="stat-label">Total withdrawn</p>
          <p className="stat-value">{formatMoney(data.totalWithdrawn)}</p>
        </article>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Recent activity</h2>
          <Link to="/transactions">View all</Link>
        </div>
        <TransactionList transactions={data.recentTransactions} />
      </section>
    </div>
  )
}
