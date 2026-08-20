import axios, { type AxiosError } from 'axios'
import type { ApiErrorBody } from './types.ts'

const TOKEN_KEY = 'wallet_access_token'

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function getApiErrorMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorBody>
    const data = axiosError.response?.data
    if (data?.errors) {
      const first = Object.values(data.errors)[0]
      if (first) return first
    }
    if (data?.message) return data.message
    if (axiosError.message) return axiosError.message
  }
  if (error instanceof Error) return error.message
  return fallback
}

export default apiClient
