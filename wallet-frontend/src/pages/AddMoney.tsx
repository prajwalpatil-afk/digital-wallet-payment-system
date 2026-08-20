import { useState, type FormEvent } from 'react'
import { getApiErrorMessage } from '../api/client.ts'
import * as walletApi from '../api/wallet.ts'
import { ErrorAlert, SuccessAlert } from '../components/Alert.tsx'
import { useAuth } from '../context/AuthContext.tsx'
import { formatMoney, toNumber } from '../lib/format.ts'
import { openRazorpayCheckout } from '../lib/razorpay.ts'

export function AddMoneyPage() {
  const { user } = useAuth()
  const [amount, setAmount] = useState('500')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [balance, setBalance] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSuccess(null)

    const value = toNumber(amount)
    if (value === null || value < 1) {
      setError('Enter an amount of at least ₹1.00')
      return
    }

    setSubmitting(true)
    try {
      const order = await walletApi.createAddMoneyOrder(value)
      const amountPaise = Math.round(Number(order.amount) * 100)

      await openRazorpayCheckout({
        key: order.keyId,
        amount: amountPaise,
        currency: order.currency,
        name: 'PayVault',
        description: 'Add money to wallet',
        order_id: order.orderId,
        prefill: {
          name: user?.name,
          email: user?.email,
        },
        theme: { color: '#0f766e' },
        handler: (response) => {
          void (async () => {
            try {
              const wallet = await walletApi.verifyAddMoney(
                response.razorpay_order_id,
                response.razorpay_payment_id,
                response.razorpay_signature,
              )
              setBalance(formatMoney(wallet.balance))
              setSuccess(`Payment verified. New balance: ${formatMoney(wallet.balance)}`)
            } catch (err) {
              setError(getApiErrorMessage(err, 'Payment verification failed'))
            } finally {
              setSubmitting(false)
            }
          })()
        },
        modal: {
          ondismiss: () => {
            setSubmitting(false)
            setError('Payment cancelled')
          },
        },
      })
    } catch (err) {
      setError(getApiErrorMessage(err, 'Could not start payment'))
      setSubmitting(false)
    }
  }

  return (
    <div className="page narrow">
      <header className="page-header">
        <div>
          <h1>Add money</h1>
          <p className="muted">Pay via Razorpay sandbox. Balance updates only after server verification.</p>
        </div>
      </header>

      <form className="panel stack-form" onSubmit={handleSubmit}>
        <ErrorAlert message={error} />
        <SuccessAlert message={success} />
        {balance && <p className="muted">Latest balance: {balance}</p>}

        <label>
          Amount (INR)
          <input
            type="number"
            min="1"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </label>

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Waiting for payment…' : 'Pay with Razorpay'}
        </button>
      </form>
    </div>
  )
}
