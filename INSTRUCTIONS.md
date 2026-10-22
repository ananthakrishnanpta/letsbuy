# Running Letsbuy

Letsbuy is one Spring Boot API and one React storefront, with PostgreSQL as the only database. Run them together in development. In production, put TLS in front of a single site so the browser talks to one origin: the shop at `/` and the API at `/api`.

Requirements:

| Tool | Version |
| --- | --- |
| Java | 21 |
| Node.js | 22 |
| PostgreSQL | 16 |
| Docker | optional, used for Postgres and for the production compose file |

The backend ships a Maven wrapper (`backend/mvnw`). You do not need a system Maven install.

## Development

### 1. Start PostgreSQL

From the repository root:

```bash
docker compose up -d
```

That starts Postgres 16 on `localhost:5432` with database `letsbuy`, user `letsbuy`, and password `letsbuy`. Data is kept in the `letsbuy-pg` volume.

Without Docker, create the same database yourself:

```sql
CREATE USER letsbuy WITH PASSWORD 'letsbuy';
CREATE DATABASE letsbuy OWNER letsbuy;
```

If your local username, password, or port differ, set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` before starting the API.

### 2. Start the API

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

The `local` profile is for a developer machine. It:

- supplies a development JWT signing key
- turns off email verification and the Secure cookie flag so `http://localhost` works
- prints verification and password-reset links in the API log
- allows the browser origins `http://localhost:5173` and `http://127.0.0.1:5173`
- creates `admin@letsbuy.dev` / `Local-Admin-1` if that user does not exist
- seeds three categories and eight sample products if the catalogue is empty

Flyway applies `backend/src/main/resources/db/migration` on startup. Hibernate then validates the schema. It does not create or alter tables.

The API listens on http://localhost:8080.

- Shop API: http://localhost:8080/api/v1
- OpenAPI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Swagger is enabled only in the `local` profile.

### 3. Start the storefront

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Vite proxies `/api` to `http://localhost:8080`, so the refresh cookie stays on the storefront origin. Use `localhost` or `127.0.0.1` consistently. Both are allowed in the local profile.

Sign in as `admin@letsbuy.dev` / `Local-Admin-1` and open Studio to manage products. A new registration in this profile is active immediately and can check out with cash on delivery.

### 4. Checks before you push

```bash
cd backend && ./mvnw verify
cd frontend && npm run build
```

`./mvnw verify` runs the auth, checkout, and module-boundary tests against an in-memory database. It does not need the Docker Postgres instance. `npm run build` typechecks the storefront and writes `frontend/dist`.

GitHub Actions runs the same two commands on push and on pull requests.

### Local environment variables

You rarely need these. Set them when the defaults are wrong.

| Variable | Default in the local profile |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | set this to `local` |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/letsbuy` |
| `DATABASE_USERNAME` | `letsbuy` |
| `DATABASE_PASSWORD` | `letsbuy` |
| `PORT` | `8080` |

Stop the database with `docker compose stop`. `docker compose down -v` also deletes the volume and the seeded data.

## Production

The default Spring profile is the production posture: email verification is required, the refresh cookie is marked Secure, email tokens are not written to logs, the catalogue is not seeded, and Swagger is off. The process refuses to start until `JWT_SECRET` is set.

Generate a signing key and keep it. Changing it signs out every current access token.

```bash
openssl rand -base64 32
```

The value must be base64 that decodes to at least 32 bytes. `openssl rand -base64 32` does that.

### First boot with Docker Compose

From the repository root, with a real admin password of at least 10 characters containing a letter and a number:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export APP_ADMIN_EMAIL=you@example.com
export APP_ADMIN_PASSWORD='a-long-password-1'
export APP_SEED_ENABLED=true
export PUBLIC_BASE_URL=https://shop.example.com
export APP_AUTH_COOKIE_SECURE=true
docker compose -f docker-compose.prod.yml up --build -d
```

Compose builds two images:

- `backend` runs `letsbuy-api.jar` on port 8080 inside the network. It is not published to the host.
- `web` builds the React app and serves it with nginx on host port 8088. nginx serves the storefront and proxies `/api/` to `backend:8080`.

Open http://localhost:8088 only as a smoke test on the same machine. A Secure cookie is not stored on plain HTTP, so sign-in will not survive a refresh until TLS is in front of the site. For that smoke test only, set `APP_AUTH_COOKIE_SECURE=false`, then set it back to `true` before real traffic.

The admin user is created once, and only when `APP_SEED_ENABLED=true` and that email does not already exist. After the first successful boot:

```bash
export APP_SEED_ENABLED=false
docker compose -f docker-compose.prod.yml up -d
```

Sample products are not loaded in production. Add categories and products from Studio, or import them with the admin API. Set `APP_SEED_CATALOG=true` only for a public demo, and only while `APP_SEED_ENABLED=true`.

### What you must change before a public launch

1. Put TLS on the public hostname. Point `PUBLIC_BASE_URL` at that `https://` origin. The API trusts `X-Forwarded-Proto` from the proxy.
2. Replace the Compose database password. The file ships with `letsbuy` / `letsbuy` so a demo can boot. Set `POSTGRES_PASSWORD` and the matching `DATABASE_PASSWORD`.
3. Decide how verification mail is delivered. Production requires a verified email before login, and the built-in sender only records that a message was queued. It does not reveal the link. Until you implement `com.letsbuy.identity.email.TransactionalEmail` with a real provider, set `APP_AUTH_REQUIRE_EMAIL_VERIFICATION=false`. The links in those emails use `PUBLIC_BASE_URL`.
4. Keep `JWT_SECRET`, the database password, and `APP_ADMIN_PASSWORD` in the host's secret store. Do not commit them and do not reuse the `local` profile.

Health for a load balancer or orchestrator:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`

Readiness includes the database.

### Production environment variables

| Variable | Required | Purpose |
| --- | --- | --- |
| `JWT_SECRET` | yes | Base64 HMAC key, at least 32 decoded bytes |
| `SPRING_PROFILES_ACTIVE` | yes for Compose | `prod` |
| `DATABASE_URL` | yes outside Compose | JDBC URL. Compose sets `jdbc:postgresql://postgres:5432/letsbuy` |
| `DATABASE_USERNAME` | yes | Database user |
| `DATABASE_PASSWORD` | yes | Database password |
| `PUBLIC_BASE_URL` | yes | Public site origin used in email links, such as `https://shop.example.com` |
| `APP_AUTH_COOKIE_SECURE` | yes | `true` behind HTTPS. `false` only for an HTTP smoke test |
| `APP_AUTH_REQUIRE_EMAIL_VERIFICATION` | no | Defaults to `true`. Set `false` until a mail provider is wired |
| `APP_ADMIN_EMAIL` | first boot | Creates this admin if the email is missing |
| `APP_ADMIN_PASSWORD` | first boot | Password for that admin |
| `APP_SEED_ENABLED` | first boot | `true` only while creating the first admin |
| `APP_SEED_CATALOG` | no | `true` loads the sample catalogue. Leave unset in production |
| `PORT` | no | API port. Defaults to `8080` |

More than one API instance can run behind one proxy. Access tokens are not stored on the server, and refresh tokens plus login lockouts live in Postgres. Sessions do not need sticky routing. Use one shared `JWT_SECRET` and one database.

Flyway runs on every API start. Ship schema changes as the next `V4__...sql` file. Do not edit a migration that has already run.

## Hosting

The storefront and the API need to share a site. The refresh token cookie is `SameSite=Lax`, HttpOnly, and scoped to `/api/v1/auth`. A shop on `https://shop.example.com` and an API on `https://api.example.com` will not send that cookie. Terminate TLS once, and route `/api/` to Spring Boot and every other path to the built React files.

Any host below can do that. Pick by how much of the machine you want to operate.

### A virtual machine and Docker Compose

A single Linux VM is the smallest production layout: Hetzner, DigitalOcean, Linode, Lightsail, or a plain EC2 instance. Install Docker, copy the repo, set the environment variables, and run `docker compose -f docker-compose.prod.yml up --build -d`.

Put Caddy or host nginx on ports 80 and 443. Proxy the public hostname to `127.0.0.1:8088`. Caddy can obtain the certificate for you. Example:

```
shop.example.com {
    reverse_proxy 127.0.0.1:8088
}
```

Take Postgres backups from the `letsbuy-pg` volume, or dump on a schedule:

```bash
docker compose -f docker-compose.prod.yml exec postgres \
  pg_dump -U letsbuy letsbuy > letsbuy-$(date +%F).sql
```

This is the option the repository is ready for today.

### A platform that runs containers

Render, Railway, Fly.io, and DigitalOcean App Platform can each run the two images plus a managed Postgres database.

- Build `backend/Dockerfile` as a private web service. Give it `JWT_SECRET`, `DATABASE_URL`, and the `APP_*` variables. Do not publish it as the public URL.
- Build `frontend/Dockerfile` as the public web service. Its nginx config looks up a host named `backend`. On these platforms, set that hostname to the private address of the API service, or change `frontend/nginx.conf` to the platform's internal URL before building.
- Use the platform's Postgres. Run one API instance first. Add instances later. They share the database and the signing key.

Set the public service's domain to `PUBLIC_BASE_URL`, and turn on the platform's HTTPS. Leave `APP_AUTH_COOKIE_SECURE=true`.

### Split static hosting and an application host

Build the storefront with `npm run build` and serve `frontend/dist` from S3 plus CloudFront, nginx, or any static host. Run the jar with `java -jar letsbuy-api.jar` on ECS, Cloud Run, App Runner, Elastic Beanstalk, or a systemd service. Put a managed Postgres such as RDS, Cloud SQL, or Neon behind it.

The edge still has to be one hostname. A typical CloudFront or nginx rule is:

- `/api/*` and `/actuator/*` go to the Spring Boot origin
- everything else goes to the static files, with unknown paths rewritten to `index.html`

Forward `X-Forwarded-Proto` and `Host`. The API is configured with `server.forward-headers-strategy: framework`.

A systemd unit only needs Java 21 and the fat jar:

```bash
cd backend
./mvnw -DskipTests package
SPRING_PROFILES_ACTIVE=prod java -jar target/letsbuy-api.jar
```

Run that under a service account with the environment variables in an environment file that is not in git.

### What this repo does not need

Kubernetes, a service mesh, and a separate auth or catalogue service are extra moving parts for this codebase. One database, one API process, and one web origin scale this shop. Add a second API replica when CPU or request volume calls for it, not before.

## Day-two operation

- Watch `GET /actuator/health`. A database outage fails readiness.
- Back up Postgres before every migration.
- Rotate `JWT_SECRET` only with a planned sign-out. Existing access tokens stop validating immediately. Refresh tokens in the database still rotate, and the next refresh issues tokens with the new key.
- To plug in email, add a `@Component` that implements `TransactionalEmail` and is active when the profile is not `test`. The local logging sender is already disabled for the `test` profile.
- Payment providers write `provider` and `provider_reference` on the existing `payments` row. The checkout flow stores no card data.
- Logs and actuator responses omit stack traces. Keep application logs on the host or ship them to the platform's log drain.
