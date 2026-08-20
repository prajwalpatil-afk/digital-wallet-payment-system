export function formatMoney(value: number | string): string {
  const amount = typeof value === 'string' ? Number(value) : value
  if (Number.isNaN(amount)) return '₹0.00'
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)
}

export function formatDateTime(iso: string): string {
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(iso))
}

export function transactionLabel(type: string): string {
  switch (type) {
    case 'DEPOSIT':
      return 'Deposit'
    case 'WITHDRAWAL':
      return 'Withdrawal'
    case 'TRANSFER_OUT':
      return 'Transfer out'
    case 'TRANSFER_IN':
      return 'Transfer in'
    default:
      return type
  }
}

export function toNumber(value: string): number | null {
  const trimmed = value.trim()
  if (!trimmed) return null
  const n = Number(trimmed)
  if (!Number.isFinite(n)) return null
  return n
}
