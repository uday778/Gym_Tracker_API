# 🏋️ Gym Tracker & Member Subscription Management API

Production-grade Spring Boot REST API for gym member management, workout tracking, and subscription lifecycle control.

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Java 17 + Spring Boot 3.3 |
| Database | MySQL + Spring Data JPA |
| Security | Spring Security 6 + JWT (JJWT 0.12) |
| Cache | Redis + Spring Cache (`@Cacheable`) |
| Rate Limiting | Bucket4j (token-bucket algorithm) |
| Documentation | Springdoc OpenAPI + Swagger UI |
| Build | Maven |
| Deployment | Docker (multi-stage) + Render |

---

## Project Structure

```
src/main/java/com/gymtracker/
├── GymTrackerApplication.java
├── config/
│   ├── SecurityConfig.java      ← Stateless JWT + RBAC URL patterns
│   ├── RedisConfig.java         ← Per-cache TTL configuration
│   └── OpenApiConfig.java       ← Swagger Bearer auth setup
├── controller/
│   ├── AuthController.java      ← POST /api/v1/auth/register|login
│   ├── MemberController.java    ← CRUD /api/v1/members
│   ├── SubscriptionController.java ← Lifecycle /api/v1/subscriptions
│   └── WorkoutController.java   ← Templates + logs /api/v1/workouts
├── service/                     ← All business logic + @Transactional
├── repository/                  ← Spring Data JPA interfaces
├── domain/                      ← JPA @Entity classes (never exposed via REST)
├── dto/
│   ├── request/                 ← Jakarta-validated request bodies
│   └── response/                ← Clean response shapes
├── exception/
│   ├── GlobalExceptionHandler.java  ← @RestControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── UserAlreadyExistsException.java
│   └── InvalidSubscriptionStateException.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── filter/
    └── RateLimitFilter.java     ← 60 req/min per user/IP
```

---

## Running Locally

### Prerequisites
- Java 17+
- MySQL 8+ (create a database named `gymtracker`)
- Redis 7+ running on `localhost:6379`

### Steps

```bash
# 1. Clone
git clone https://github.com/your-username/gym-tracker.git
cd gym-tracker

# 2. Set environment variables (or edit application.yml directly for local dev)
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/gymtracker?useSSL=false&serverTimezone=UTC
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=yourpassword
export JWT_SECRET=dGhpcy1pcy1hLXZlcnktbG9uZy1hbmQtc2VjdXJlLXNlY3JldC1rZXktZm9yLWd5bS10cmFja2Vy

# 3. Build and run
mvn spring-boot:run
```

### Swagger UI
Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

1. Call `POST /api/v1/auth/register` to create an ADMIN user.
2. Call `POST /api/v1/auth/login` to get a JWT token.
3. Click **Authorize** → paste `Bearer <token>`.
4. All protected endpoints are now accessible.

---

## Running with Docker Compose (local)

```yaml
# docker-compose.yml (create in project root for local testing)
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: gymtracker
      MYSQL_ROOT_PASSWORD: root
    ports: ["3306:3306"]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  app:
    build: .
    ports: ["8080:8080"]
    depends_on: [mysql, redis]
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/gymtracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      JWT_SECRET: dGhpcy1pcy1hLXZlcnktbG9uZy1hbmQtc2VjdXJlLXNlY3JldC1rZXktZm9yLWd5bS10cmFja2Vy
```

```bash
docker compose up --build
```

---

## Deploying to Render

```bash
# 1. Push to GitHub
git push origin main

# 2. In Render dashboard:
#    New → Blueprint → connect repo → Render reads render.yaml → Deploy

# 3. All environment variables are auto-wired from render.yaml.
#    Only JWT_SECRET needs to be confirmed (render generates it automatically).
```

---

## API Endpoints Summary

### Auth
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | Public | Register user |
| POST | `/api/v1/auth/login` | Public | Login, receive JWT |

### Members
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/members` | ADMIN | Create member profile |
| GET | `/api/v1/members` | ADMIN, TRAINER | List active members (paginated) |
| GET | `/api/v1/members/search?query=` | ADMIN, TRAINER | Search members |
| GET | `/api/v1/members/{id}` | ALL | Get member by ID |
| PUT | `/api/v1/members/{id}` | ADMIN, TRAINER | Update member |
| DELETE | `/api/v1/members/{id}` | ADMIN | Deactivate member |

### Subscriptions
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/subscriptions` | ADMIN | Create subscription |
| GET | `/api/v1/subscriptions?status=ACTIVE` | ADMIN | List by status (paginated, cached) |
| GET | `/api/v1/subscriptions/member/{id}` | ADMIN, MEMBER | Member's subscriptions |
| GET | `/api/v1/subscriptions/{id}` | ADMIN, MEMBER | Get by ID |
| PATCH | `/api/v1/subscriptions/{id}/cancel` | ADMIN | Cancel |
| PATCH | `/api/v1/subscriptions/{id}/pause` | ADMIN | Pause |
| PATCH | `/api/v1/subscriptions/{id}/resume` | ADMIN | Resume |

### Workouts
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/workouts/templates` | ALL | List templates (cached) |
| GET | `/api/v1/workouts/templates/category/{cat}` | ALL | By category (paginated) |
| GET | `/api/v1/workouts/templates/{id}` | ALL | Get template |
| POST | `/api/v1/workouts/templates` | TRAINER, ADMIN | Create template |
| PUT | `/api/v1/workouts/templates/{id}` | TRAINER, ADMIN | Update template |
| DELETE | `/api/v1/workouts/templates/{id}` | ADMIN | Deactivate template |
| POST | `/api/v1/workouts/logs` | ALL | Log a workout |
| GET | `/api/v1/workouts/logs/member/{id}` | ALL | Member's logs (paginated) |
| GET | `/api/v1/workouts/logs/member/{id}/range` | ALL | Logs by date range |
| GET | `/api/v1/workouts/logs/{id}` | ALL | Get log |
| DELETE | `/api/v1/workouts/logs/{id}` | MEMBER, ADMIN | Delete log |

---

## Key Design Decisions

### 1. Stateless JWT over Session-Based Auth
JWT tokens are stateless — any pod can verify them without a shared session store. This enables horizontal scaling. Tokens carry the role claim, so no DB hit is needed per request.

### 2. Two-Layer RBAC
Security is enforced at both URL pattern level (`SecurityConfig`) and method level (`@PreAuthorize`). This provides defence-in-depth — if a URL pattern is accidentally widened, the method-level check still blocks unauthorised access.

### 3. Redis Caching with Explicit Eviction
`@Cacheable` caches expensive list queries (workout templates, subscription tiers). `@CacheEvict(allEntries = true)` on every write ensures cache coherency. Per-cache TTL overrides (1 hour for subscription tiers, 30 min for templates) balance freshness with performance.

### 4. Optimistic Locking (`@Version`)
All mutable entities carry a `@Version` field. Hibernate uses this for optimistic locking — concurrent updates to the same row raise `OptimisticLockException` instead of silently overwriting each other. This is the standard approach for preventing lost updates in a multi-user environment.

### 5. Never Leak Stack Traces
`server.error.include-stacktrace: never` and `server.error.include-message: never` are set in `application.yml`. All exceptions are caught by `GlobalExceptionHandler`, which maps them to a clean, structured `ApiErrorResponse` with a timestamp, status code, errorCode string, and user-friendly message.

---

## Running Tests

```bash
mvn test
```

Tests use Mockito for isolated unit testing of the service layer — no database or Redis required.
