import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client.ts'
import type { WalletResponse } from '../api/types.ts'
import * as walletApi from '../api/wallet.ts'
import { ErrorAlert } from '../components/Alert.tsx'
import { formatDateTime, formatMoney } from '../lib/format.ts'

export function WalletPage() {
  const [wallet, setWallet] = useState<WalletResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const data = await walletApi.getWallet()
        if (!cancelled) setWallet(data)
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load wallet'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void load()
    return () => {
      cancelled = true
    }
  }, [])

  if (loading) return <p className="muted">Loading wallet…</p>
  if (error) return <ErrorAlert message={error} />
  if (!wallet) return null

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Wallet</h1>
          <p className="muted">Wallet #{wallet.id}</p>
        </div>
      </header>

      <section className="panel wallet-hero">
        <p className="stat-label">Current balance</p>
        <p className="balance-xl">{formatMoney(wallet.balance)}</p>
        <dl className="meta-grid">
          <div>
            <dt>Created</dt>
            <dd>{formatDateTime(wallet.createdAt)}</dd>
          </div>
          <div>
            <dt>Last updated</dt>
            <dd>{formatDateTime(wallet.updatedAt)}</dd>
          </div>
        </dl>
        <div className="action-row">
          <Link className="btn btn-primary" to="/add-money">
            Add money
          </Link>
          <Link className="btn btn-secondary" to="/withdraw">
            Withdraw
          </Link>
          <Link className="btn btn-secondary" to="/transfer">
            Transfer
          </Link>
        </div>
      </section>
    </div>
  )
}
