import apiClient from './client.ts'
import type {
  CreateOrderResponse,
  DashboardResponse,
  TransactionResponse,
  WalletResponse,
} from './types.ts'

export async function getWallet(): Promise<WalletResponse> {
  const { data } = await apiClient.get<WalletResponse>('/wallet')
  return data
}

export async function getDashboard(): Promise<DashboardResponse> {
  const { data } = await apiClient.get<DashboardResponse>('/wallet/dashboard')
  return data
}

export async function getTransactions(): Promise<TransactionResponse[]> {
  const { data } = await apiClient.get<TransactionResponse[]>('/wallet/transactions')
  return data
}

export async function withdraw(amount: number): Promise<WalletResponse> {
  const { data } = await apiClient.post<WalletResponse>('/wallet/withdraw', { amount })
  return data
}

export async function transfer(recipientEmail: string, amount: number): Promise<WalletResponse> {
  const { data } = await apiClient.post<WalletResponse>('/wallet/transfer', {
    recipientEmail,
    amount,
  })
  return data
}

export async function createAddMoneyOrder(amount: number): Promise<CreateOrderResponse> {
  const { data } = await apiClient.post<CreateOrderResponse>('/wallet/add-money/order', { amount })
  return data
}

export async function verifyAddMoney(
  razorpayOrderId: string,
  razorpayPaymentId: string,
  razorpaySignature: string,
): Promise<WalletResponse> {
  const { data } = await apiClient.post<WalletResponse>('/wallet/add-money/verify', {
    razorpayOrderId,
    razorpayPaymentId,
    razorpaySignature,
  })
  return data
}
