# FluxBanker API Reference

This document provides a comprehensive overview of the FluxBanker API endpoints, authentication flows, and usage examples.

## Interactive Documentation (Swagger UI)

When the backend is running, you can access the interactive Swagger UI at:
- **URL**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Authentication

FluxBanker uses JWT-based authentication. Most endpoints require a valid Bearer token in the `Authorization` header.

### 1. Register a new user
`POST /api/v1/auth/register`

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "Password123!",
    "address1": "123 MG Road",
    "city": "Mumbai",
    "aadhaar": "123456789012",
    "dateOfBirth": "1990-01-01",
    "pinCode": "400001",
    "state": "MH"
  }'
```

### 2. Login
`POST /api/v1/auth/login`

Returns an `accessToken` and sets a `refreshToken` in a secure HTTP-only cookie.

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123!"
  }'
```

---

## Endpoints Summary

### Authentication (`/api/v1/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| POST | `/register` | Create a new system account |
| POST | `/login` | Authenticate and get JWT |
| POST | `/logout` | Invalidate current session |
| POST | `/refresh` | Get a new JWT using refresh cookie |

### Accounts (`/api/v1/accounts`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| GET | `/` | List all accounts for the authenticated user |
| POST | `/mock` | Provision a new simulated banking account |
| POST | `/{id}/deposit` | Simulate a cash deposit into an account |

### Transactions (`/api/v1/transactions`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| GET | `/account/{id}` | Get transaction history for a specific account |
| POST | `/transfer` | Execute an ACID-compliant transfer between accounts |

### User Profile (`/api/v1/users`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| GET | `/me` | Get current user profile and role |

### Admin Control (`/api/v1/admin`)
*Requires `ADMIN` role*

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| GET | `/users` | List all registered users (paginated) |
| GET | `/transactions` | List global ledger activity (paginated) |

---

## Detailed Usage Examples (cURL)

### Provision a Mock Account
```bash
curl -X POST http://localhost:8080/api/v1/accounts/mock \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Primary Savings",
    "subtype": "SAVINGS"
  }'
```

### Execute a Transfer
```bash
curl -X POST http://localhost:8080/api/v1/transactions/transfer \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": "uuid-here",
    "destinationAccountId": "uuid-here",
    "amount": 500.00
  }'
```

### Check System Health
```bash
curl http://localhost:8080/api/v1/health
```

---

## System Design Notes
- **Ledger Integrity**: All transfers are processed within database transactions to ensure consistency.
- **Audit Logs**: Every account movement is recorded in the global ledger available to administrators.
- **Pagination**: Admin endpoints return Spring Data `Page` objects containing `content`, `totalPages`, and `totalElements`.
