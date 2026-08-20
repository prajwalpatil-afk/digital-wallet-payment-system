export interface RazorpaySuccessResponse {
  razorpay_payment_id: string
  razorpay_order_id: string
  razorpay_signature: string
}

export interface RazorpayCheckoutOptions {
  key: string
  amount: number
  currency: string
  name?: string
  description?: string
  order_id: string
  handler: (response: RazorpaySuccessResponse) => void
  prefill?: {
    name?: string
    email?: string
  }
  theme?: {
    color?: string
  }
  modal?: {
    ondismiss?: () => void
  }
}

interface RazorpayInstance {
  open: () => void
}

interface RazorpayConstructor {
  new (options: RazorpayCheckoutOptions): RazorpayInstance
}

declare global {
  interface Window {
    Razorpay?: RazorpayConstructor
  }
}

const SCRIPT_URL = 'https://checkout.razorpay.com/v1/checkout.js'

let scriptPromise: Promise<void> | null = null

export function loadRazorpayScript(): Promise<void> {
  if (window.Razorpay) return Promise.resolve()
  if (scriptPromise) return scriptPromise

  scriptPromise = new Promise((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>(`script[src="${SCRIPT_URL}"]`)
    if (existing) {
      existing.addEventListener('load', () => resolve())
      existing.addEventListener('error', () => reject(new Error('Failed to load Razorpay')))
      return
    }

    const script = document.createElement('script')
    script.src = SCRIPT_URL
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => {
      scriptPromise = null
      reject(new Error('Failed to load Razorpay Checkout'))
    }
    document.body.appendChild(script)
  })

  return scriptPromise
}

export async function openRazorpayCheckout(options: RazorpayCheckoutOptions): Promise<void> {
  await loadRazorpayScript()
  if (!window.Razorpay) {
    throw new Error('Razorpay SDK unavailable')
  }
  const rzp = new window.Razorpay(options)
  rzp.open()
}
