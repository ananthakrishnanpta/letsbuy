# Letsbuy

A modular-monolith shop: a React storefront and one Spring Boot API, backed by PostgreSQL. Features stay in their own packages on both sides. There is no microservice split.

## Layout

```
backend/src/main/java/com/letsbuy
  common/          shared API envelope, errors, money, slugs
  security/        JWT access tokens, refresh cookie, filter chain
  identity/        users, roles, sessions, verification, password reset
  catalog/         categories, products, stock
  cart/
  ordering/        addresses, checkout, orders, payments
frontend/src
  app/             router and providers
  shared/          http client, shell, design primitives
  features/<name>/{api,components,hooks,pages,types}
```

A feature may call another feature only through its `api` package (`ProductLookup`, `InventoryPort`, `CartPort`). ArchUnit checks that boundary.

## Auth

Passwords are BCrypt. Access tokens are short-lived HS256 JWTs kept in memory. Refresh tokens are random, stored only as SHA-256 hashes, rotated on every use, and bound to a family so a reused token revokes the whole family. The refresh token travels in an HttpOnly `SameSite=Lax` cookie on `/api/v1/auth`. Refresh and logout require the `X-Letsbuy-Request` header so a cross-site form cannot spend the cookie.

Failed logins are counted on the user row (safe across instances) and the account locks for 15 minutes after 5 failures. Unknown emails and bad passwords return the same error. Reset and verification links store a hash, expire, and are single use. Password reset revokes every refresh token.

Email delivery is an interface. The default sender logs the link when `app.auth.log-email-tokens` is true (local only). Plug in a real provider by implementing `TransactionalEmail`.

Production defaults require email verification, a secure cookie, and `JWT_SECRET` (base64, at least 32 bytes). The `local` profile relaxes that and seeds a catalogue plus `admin@letsbuy.dev` / `Local-Admin-1`.

## Money and checkout

Prices are integer minor units (paise) in `INR`. Checkout locks each product row, snapshots the price onto the order line, decrements stock, and clears the cart in one transaction. Shipping is ₹49, or free from ₹999. The only payment method is cash on delivery: a `payments` row is created as `PENDING` and moves to `CAPTURED` when an admin fulfills the order. No card data is stored. A gateway can later write `provider` and `provider_reference` on that row.

## Run locally

Day-to-day commands, production variables, and hosting options are in [INSTRUCTIONS.md](INSTRUCTIONS.md).

PostgreSQL 16, Java 21, Node 22.

```bash
docker compose up -d
cd backend && SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
cd frontend && npm install && npm run dev
```

The Vite dev server proxies `/api` to `localhost:8080`, so the refresh cookie stays on one origin. The shop is at http://localhost:5173.

OpenAPI is on http://localhost:8080/swagger-ui.html in the local profile and disabled in `prod`.

## Production shape

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export APP_ADMIN_EMAIL=you@example.com
export APP_ADMIN_PASSWORD='a-long-password-1'
export APP_SEED_ENABLED=true
docker compose -f docker-compose.prod.yml up --build
```

Put TLS in front of the web container and set `APP_AUTH_COOKIE_SECURE=true`. The admin seed runs once, only when that email does not exist yet. Turn `APP_SEED_ENABLED` off after the first boot.

Health is at `/actuator/health`.
