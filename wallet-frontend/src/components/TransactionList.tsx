import type { TransactionResponse } from '../api/types.ts'
import { formatDateTime, formatMoney, transactionLabel } from '../lib/format.ts'

interface Props {
  transactions: TransactionResponse[]
  emptyMessage?: string
}

export function TransactionList({ transactions, emptyMessage = 'No transactions yet.' }: Props) {
  if (transactions.length === 0) {
    return <p className="muted empty-state">{emptyMessage}</p>
  }

  return (
    <ul className="txn-list">
      {transactions.map((txn) => {
        const outbound = txn.type === 'WITHDRAWAL' || txn.type === 'TRANSFER_OUT'
        return (
          <li key={txn.id} className="txn-row">
            <div>
              <p className="txn-type">{transactionLabel(txn.type)}</p>
              <p className="txn-meta">
                {formatDateTime(txn.createdAt)}
                {txn.description ? ` · ${txn.description}` : ''}
              </p>
            </div>
            <div className="txn-right">
              <p className={outbound ? 'amount-out' : 'amount-in'}>
                {outbound ? '−' : '+'}
                {formatMoney(txn.amount)}
              </p>
              <span className={`status-pill status-${txn.status.toLowerCase()}`}>{txn.status}</span>
            </div>
          </li>
        )
      })}
    </ul>
  )
}
