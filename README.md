# Digital Wallet & Payment System (Stage 1)

Portfolio / resume project — **not real money, not a production banking system**.

Stage 1 is a monolith: one Spring Boot backend owns auth + wallet, one React frontend, one PostgreSQL database. Stage 2 (Auth Service extraction) starts only after this is demoable.

For the full build plan and design decisions, see [`../digital-wallet-execution-plan.md`](../digital-wallet-execution-plan.md).

---

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, JPA |
| Auth | BCrypt passwords, JWT (HS256), access-token only |
| Payments | Razorpay Checkout (test/sandbox mode) + server-side HMAC verification |
| Database | PostgreSQL (`wallet_db`), `ddl-auto=update` (no Flyway) |
| Frontend | React 19, TypeScript, Vite, Tailwind, React Router, Axios |

---

## Project layout

```
wallet/
├── wallet-backend/     Spring Boot API (auth, wallet, admin, Razorpay)
└── wallet-frontend/    React SPA
```

---

## Prerequisites

- Java 21+
- Maven (or use included `mvnw`)
- Node 20+
- PostgreSQL running locally
- A Razorpay **test-mode** key pair ([Dashboard → API Keys](https://dashboard.razorpay.com/app/keys))

---

## Local setup

### 1. Database

Create an empty database:

```sql
CREATE DATABASE wallet_db;
```

### 2. Backend

```bash
cd wallet-backend
cp .env.example .env
# Edit .env — set DB_PASSWORD, JWT_SECRET (≥32 chars), Razorpay test keys
```

Load env vars for your shell (example for PowerShell after editing `.env`):

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*#' -or $_ -match '^\s*$') { return }
  $k, $v = $_ -split '=', 2
  Set-Item -Path "Env:$($k.Trim())" -Value $v.Trim()
}
.\mvnw.cmd spring-boot:run
```

API: `http://localhost:8080`

### 3. Frontend

```bash
cd wallet-frontend
cp .env.example .env   # optional — defaults to http://localhost:8080/api
npm install
npm run dev
```

App: `http://localhost:5173`

### 4. Seed an admin (manual)

Registration always creates `USER`. Promote one account in SQL after registering:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

Log out and log in again so the JWT picks up the new role.

---

## Environment variables

### Backend (`wallet-backend/.env`)

| Variable | Purpose |
|---|---|
| `DB_USERNAME` / `DB_PASSWORD` | PostgreSQL credentials |
| `JWT_SECRET` | HS256 signing secret (≥32 characters) |
| `JWT_ACCESS_TTL_SECONDS` | Access token lifetime (default `3600`) |
| `FRONTEND_URL` | Allowed CORS origin (default `http://localhost:5173`) |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | Razorpay **test** keys — secret stays server-side only |

### Frontend (`wallet-frontend/.env`)

| Variable | Purpose |
|---|---|
| `VITE_API_BASE_URL` | Backend API base (default `http://localhost:8080/api`) |

---

## API overview

**Auth** (public)

- `POST /api/auth/register`
- `POST /api/auth/login`

**Wallet** (JWT required)

- `GET /api/wallet`
- `GET /api/wallet/dashboard`
- `POST /api/wallet/add-money/order`
- `POST /api/wallet/add-money/verify`
- `POST /api/wallet/withdraw`
- `POST /api/wallet/transfer`
- `GET /api/wallet/transactions`

**Admin** (JWT + `ADMIN` role)

- `GET /api/admin/users`
- `GET /api/admin/transactions`

Error responses use a consistent JSON shape:

```json
{ "status": 400, "message": "…", "timestamp": "…", "errors": { "field": "…" } }
```

(`errors` is present for validation failures only.)

---

## How to run tests

From `wallet-backend` (uses in-memory H2 — Postgres not required):

```bash
./mvnw test
# Windows: .\mvnw.cmd test
```

Coverage targets the risk areas: auth, balance correctness, authorization, and Razorpay signature verification.

---

## Demo journey (manual)

1. Register two users in the UI.
2. Log in as user A → Dashboard → Add Money via Razorpay sandbox (test cards from Razorpay docs).
3. Withdraw a small amount; Transfer to user B.
4. Check Transactions on both accounts.
5. Promote one user to `ADMIN` in SQL, re-login, open Admin (users + transactions).

---

## Design decisions (Stage 1)

Summarized from the execution plan:

- **Monolith first** — auth lives in the wallet backend so Stage 2 can delete it wholesale.
- **Access token only** — no refresh tokens yet; Stage 2 reuses the existing auth app’s refresh logic.
- **HS256 JWT** — one service both issues and validates; RS256 arrives when a second service must verify without issuing.
- **`@Transactional` only** — no optimistic locking for this demo’s concurrency profile.
- **No Flyway** — `ddl-auto=update` while iterating.
- **Recent N transactions** — no pagination/filters.
- **Token in `localStorage`** — pragmatic for a portfolio demo (refresh keeps you logged in).
- **Transaction status `SUCCESS` / `FAILED` only** — flows resolve synchronously; order creation writes no row until verify.

---

## Screenshots / GIF

Add captures under `docs/` or `screenshots/` when demoing (dashboard, add-money checkout, transfer, admin). Architecture diagrams are deferred to Stage 2 / Phase 15 once the split exists.

---

## Known limitations / out of scope

**Deferred to Stage 2+**

- Refresh tokens, RS256 JWT, auth/wallet service split, two Postgres schemas
- Optimistic locking, transaction pagination/filters
- Architecture diagrams

**Skipped entirely**

- Flyway, OAuth2 (Google/GitHub), real bank payouts, KYC/AML, fraud systems
- Docker/K8s, cloud deploy, CI/CD, Redis, Kafka, observability stacks
- PCI DSS / production banking compliance

---

## Resume bullets (Stage 1)

- Built a full-stack digital wallet (React/TypeScript, Spring Boot, PostgreSQL) with deposits, withdrawals, and P2P transfers under transactional balance updates.
- Integrated Razorpay sandbox Checkout with server-side order creation and HMAC signature verification to block client-side payment spoofing.
- Wrote a focused JUnit/MockMvc suite covering auth, RBAC, and financial edge cases (insufficient balance, invalid recipient, role gating).
