export type Role = 'USER' | 'ADMIN'

export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER_OUT' | 'TRANSFER_IN'

export type TransactionStatus = 'SUCCESS' | 'FAILED'

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresInSeconds: number
  userId: number
  name: string
  email: string
  role: Role
}

export interface UserProfile {
  id: number
  name: string
  email: string
  role: Role
  createdAt: string
}

export interface WalletResponse {
  id: number
  userId: number
  balance: number | string
  createdAt: string
  updatedAt: string
}

export interface TransactionResponse {
  id: number
  type: TransactionType
  amount: number | string
  status: TransactionStatus
  relatedWalletId: number | null
  referenceId: string | null
  description: string | null
  createdAt: string
}

export interface DashboardResponse {
  balance: number | string
  totalDeposited: number | string
  totalWithdrawn: number | string
  recentTransactions: TransactionResponse[]
}

export interface CreateOrderResponse {
  orderId: string
  keyId: string
  amount: number | string
  currency: string
}

export interface AdminTransactionResponse {
  id: number
  walletId: number
  userId: number
  userEmail: string
  type: TransactionType
  amount: number | string
  status: TransactionStatus
  relatedWalletId: number | null
  referenceId: string | null
  description: string | null
  createdAt: string
}

export interface ApiErrorBody {
  status: number
  message: string
  timestamp?: string
  errors?: Record<string, string>
}
