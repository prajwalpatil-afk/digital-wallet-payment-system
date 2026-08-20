import apiClient from './client.ts'
import type { AuthResponse, UserProfile } from './types.ts'

export async function register(name: string, email: string, password: string): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/register', { name, email, password })
  return data
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', { email, password })
  return data
}

export async function fetchMe(): Promise<UserProfile> {
  const { data } = await apiClient.get<UserProfile>('/me')
  return data
}
