import apiClient from './client.ts'
import type { AdminTransactionResponse, UserProfile } from './types.ts'

export async function listUsers(): Promise<UserProfile[]> {
  const { data } = await apiClient.get<UserProfile[]>('/admin/users')
  return data
}

export async function listAllTransactions(): Promise<AdminTransactionResponse[]> {
  const { data } = await apiClient.get<AdminTransactionResponse[]>('/admin/transactions')
  return data
}
