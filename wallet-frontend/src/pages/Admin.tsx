import { useEffect, useState } from 'react'
import * as adminApi from '../api/admin.ts'
import { getApiErrorMessage } from '../api/client.ts'
import type { AdminTransactionResponse, UserProfile } from '../api/types.ts'
import { ErrorAlert } from '../components/Alert.tsx'
import { formatDateTime, formatMoney, transactionLabel } from '../lib/format.ts'

type Tab = 'users' | 'transactions'

export function AdminPage() {
  const [tab, setTab] = useState<Tab>('users')
  const [users, setUsers] = useState<UserProfile[]>([])
  const [transactions, setTransactions] = useState<AdminTransactionResponse[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [userList, txnList] = await Promise.all([
          adminApi.listUsers(),
          adminApi.listAllTransactions(),
        ])
        if (!cancelled) {
          setUsers(userList)
          setTransactions(txnList)
        }
      } catch (err) {
        if (!cancelled) setError(getApiErrorMessage(err, 'Failed to load admin data'))
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
          <h1>Admin</h1>
          <p className="muted">Users and system-wide transactions.</p>
        </div>
      </header>

      <div className="tab-row" role="tablist">
        <button
          type="button"
          role="tab"
          className={tab === 'users' ? 'tab active' : 'tab'}
          aria-selected={tab === 'users'}
          onClick={() => setTab('users')}
        >
          Users ({users.length})
        </button>
        <button
          type="button"
          role="tab"
          className={tab === 'transactions' ? 'tab active' : 'tab'}
          aria-selected={tab === 'transactions'}
          onClick={() => setTab('transactions')}
        >
          Transactions ({transactions.length})
        </button>
      </div>

      <section className="panel">
        {loading && <p className="muted">Loading…</p>}
        <ErrorAlert message={error} />

        {!loading && !error && tab === 'users' && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Joined</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td>{u.role}</td>
                    <td>{formatDateTime(u.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!loading && !error && tab === 'transactions' && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>When</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((txn) => (
                  <tr key={txn.id}>
                    <td>{txn.id}</td>
                    <td>{txn.userEmail}</td>
                    <td>{transactionLabel(txn.type)}</td>
                    <td>{formatMoney(txn.amount)}</td>
                    <td>{txn.status}</td>
                    <td>{formatDateTime(txn.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
