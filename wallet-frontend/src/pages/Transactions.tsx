import { useEffect, useState } from 'react'
import { getApiErrorMessage } from '../api/client.ts'
import type { TransactionResponse } from '../api/types.ts'
import * as walletApi from '../api/wallet.ts'
import { ErrorAlert } from '../components/Alert.tsx'
import { TransactionList } from '../components/TransactionList.tsx'

export function TransactionsPage() {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const data = await walletApi.getTransactions()
        if (!cancelled) setTransactions(data)
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load transactions'))
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void load()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Transactions</h1>
          <p className="muted">Most recent activity on your wallet.</p>
        </div>
      </header>

      <section className="panel">
        {loading && <p className="muted">Loading…</p>}
        <ErrorAlert message={error} />
        {!loading && !error && <TransactionList transactions={transactions} />}
      </section>
    </div>
  )
}
