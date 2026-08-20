import { useState, type FormEvent } from 'react'
import { getApiErrorMessage } from '../api/client.ts'
import * as walletApi from '../api/wallet.ts'
import { ErrorAlert, SuccessAlert } from '../components/Alert.tsx'
import { formatMoney, toNumber } from '../lib/format.ts'

export function WithdrawPage() {
  const [amount, setAmount] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSuccess(null)

    const value = toNumber(amount)
    if (value === null || value <= 0) {
      setError('Enter an amount greater than 0')
      return
    }

    setSubmitting(true)
    try {
      const wallet = await walletApi.withdraw(value)
      setSuccess(`Withdrew ${formatMoney(value)}. New balance: ${formatMoney(wallet.balance)}`)
      setAmount('')
    } catch (err) {
      setError(getApiErrorMessage(err, 'Withdrawal failed'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page narrow">
      <header className="page-header">
        <div>
          <h1>Withdraw</h1>
          <p className="muted">Debit from your wallet balance.</p>
        </div>
      </header>

      <form className="panel stack-form" onSubmit={handleSubmit}>
        <ErrorAlert message={error} />
        <SuccessAlert message={success} />

        <label>
          Amount (INR)
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </label>

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Withdrawing…' : 'Withdraw'}
        </button>
      </form>
    </div>
  )
}
