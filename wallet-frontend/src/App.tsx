import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/AppLayout.tsx'
import { AuthProvider } from './context/AuthContext.tsx'
import { AddMoneyPage } from './pages/AddMoney.tsx'
import { AdminPage } from './pages/Admin.tsx'
import { DashboardPage } from './pages/Dashboard.tsx'
import { LoginPage } from './pages/Login.tsx'
import { RegisterPage } from './pages/Register.tsx'
import { TransactionsPage } from './pages/Transactions.tsx'
import { TransferPage } from './pages/Transfer.tsx'
import { WalletPage } from './pages/Wallet.tsx'
import { WithdrawPage } from './pages/Withdraw.tsx'
import { AdminRoute, GuestRoute, ProtectedRoute } from './routes/ProtectedRoute.tsx'

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route element={<GuestRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/wallet" element={<WalletPage />} />
            <Route path="/add-money" element={<AddMoneyPage />} />
            <Route path="/withdraw" element={<WithdrawPage />} />
            <Route path="/transfer" element={<TransferPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
          </Route>
        </Route>

        <Route element={<AdminRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>
        </Route>

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  )
}
