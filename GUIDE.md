# ============================================================
#  GYM TRACKER API — COMPLETE GUIDE
#  Run Steps | Testing | Redis Deep Dive | Interview Q&A
# ============================================================

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 1 — HOW TO RUN THE PROJECT (Step by Step)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

─────────────────────────────────────────
PREREQUISITES — Install These First
─────────────────────────────────────────

1. Java 17
   - Windows: https://adoptium.net → download JDK 17 → install
   - Check: open CMD → type: java -version
   - You should see: openjdk version "17.x.x"

2. Maven
   - Windows: https://maven.apache.org/download.cgi
   - Extract zip → add bin/ folder to PATH
   - Check: mvn -version

3. MySQL 8
   - Download MySQL Community Server: https://dev.mysql.com/downloads/mysql/
   - During install, set root password (remember it!)
   - Check: mysql -u root -p → enter password

4. Redis
   - Windows: Use Docker (easiest) → see Step B below
   - OR download Memurai (Redis for Windows): https://www.memurai.com
   - Check: redis-cli ping → should return PONG

5. Docker (optional but recommended)
   - https://www.docker.com/products/docker-desktop


─────────────────────────────────────────
STEP A — Set Up the Database
─────────────────────────────────────────

Open MySQL Workbench or MySQL command line:

  mysql -u root -p

Then run:

  CREATE DATABASE gymtracker;
  USE gymtracker;

That's it. Spring Boot (ddl-auto=update) will create all
tables automatically when the app starts.


─────────────────────────────────────────
STEP B — Start Redis (Two Options)
─────────────────────────────────────────

Option 1: Docker (recommended — one command)

  docker run -d --name gym-redis -p 6379:6379 redis:7-alpine

Option 2: If Redis is installed natively
  
  redis-server

Verify Redis is running:

  redis-cli ping
  # Should print: PONG


─────────────────────────────────────────
STEP C — Configure application.properties
─────────────────────────────────────────

Open: src/main/resources/application.properties

Change these three lines to match your MySQL setup:

  spring.datasource.url=jdbc:mysql://localhost:3306/gymtracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  spring.datasource.username=root
  spring.datasource.password=YOUR_MYSQL_PASSWORD_HERE

Redis default (localhost:6379) usually works without changes.
Leave JWT secret as-is for local development.


─────────────────────────────────────────
STEP D — Build and Run
─────────────────────────────────────────

In your terminal, navigate to the project folder:

  cd gym-tracker

Build the project:

  mvn clean install -DskipTests

Run the application:

  mvn spring-boot:run

You should see this in the console:
  
  Started GymTrackerApplication in X.XXX seconds

The API is now live at: http://localhost:8080


─────────────────────────────────────────
STEP E — Open Swagger UI
─────────────────────────────────────────

Open your browser and go to:

  http://localhost:8080/swagger-ui.html

You will see all your API endpoints listed.
You can test them directly from this page.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 2 — TESTING WITH DUMMY DATA (Step by Step)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Use Swagger UI (http://localhost:8080/swagger-ui.html)
OR use Postman / curl — all examples below use curl.

─────────────────────────────────────────
TEST 1 — Register an Admin User
─────────────────────────────────────────

curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Uday",
    "lastName": "Kumar",
    "email": "uday@gymtracker.com",
    "password": "Admin@1234",
    "role": "ROLE_ADMIN"
  }'

Expected Response (201 Created):
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 1,
  "email": "uday@gymtracker.com",
  "fullName": "Uday Kumar",
  "role": "ROLE_ADMIN"
}

SAVE the accessToken value. You will need it for all other requests.
Replace TOKEN below with your actual token.

─────────────────────────────────────────
TEST 2 — Register a Trainer and a Member
─────────────────────────────────────────

Register a trainer:

curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ravi",
    "lastName": "Sharma",
    "email": "ravi@gymtracker.com",
    "password": "Trainer@1234",
    "role": "ROLE_TRAINER"
  }'

Register a member:

curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Priya",
    "lastName": "Reddy",
    "email": "priya@gymtracker.com",
    "password": "Member@1234",
    "role": "ROLE_MEMBER"
  }'


─────────────────────────────────────────
TEST 3 — Login
─────────────────────────────────────────

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "uday@gymtracker.com",
    "password": "Admin@1234"
  }'

Save the accessToken from the response.


─────────────────────────────────────────
TEST 4 — Create a Member Profile (Admin)
─────────────────────────────────────────

After registering Priya as a user (userId=3 from TEST 2),
create her member profile:

curl -X POST http://localhost:8080/api/v1/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "firstName": "Priya",
    "lastName": "Reddy",
    "phone": "9876543210",
    "dateOfBirth": "1998-05-15",
    "gender": "Female",
    "address": "Hyderabad, Telangana",
    "heightCm": 162.0,
    "weightKg": 58.0,
    "userId": 3
  }'

Expected Response (201 Created):
{
  "id": 1,
  "userId": 3,
  "firstName": "Priya",
  "lastName": "Reddy",
  "fullName": "Priya Reddy",
  "email": "priya@gymtracker.com",
  "phone": "9876543210",
  ...
}


─────────────────────────────────────────
TEST 5 — Create a Subscription (Admin)
─────────────────────────────────────────

curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "memberId": 1,
    "tier": "PREMIUM",
    "startDate": "2025-06-01",
    "endDate": "2026-06-01",
    "amountPaid": 59.99,
    "notes": "Annual premium plan"
  }'

Expected Response (201 Created):
{
  "id": 1,
  "memberId": 1,
  "memberName": "Priya Reddy",
  "tier": "PREMIUM",
  "tierDisplayName": "Premium",
  "status": "ACTIVE",
  "startDate": "2025-06-01",
  "endDate": "2026-06-01",
  "amountPaid": 59.99
}


─────────────────────────────────────────
TEST 6 — Create a Workout Template (Trainer)
─────────────────────────────────────────

Login as Ravi (trainer) first and get his token, then:

curl -X POST http://localhost:8080/api/v1/workouts/templates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TRAINER_TOKEN" \
  -d '{
    "name": "Full Body Strength",
    "description": "Complete full body workout for beginners",
    "category": "STRENGTH",
    "durationMinutes": 60,
    "caloriesBurned": 400,
    "difficultyLevel": "BEGINNER",
    "exercisesJson": "[{\"name\":\"Squats\",\"sets\":3,\"reps\":12},{\"name\":\"Push-ups\",\"sets\":3,\"reps\":10}]"
  }'


─────────────────────────────────────────
TEST 7 — Log a Workout (Member)
─────────────────────────────────────────

curl -X POST http://localhost:8080/api/v1/workouts/logs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "memberId": 1,
    "workoutName": "Morning Cardio Session",
    "category": "CARDIO",
    "logDate": "2025-06-01",
    "durationMinutes": 45,
    "caloriesBurned": 350,
    "notes": "Felt great today!"
  }'


─────────────────────────────────────────
TEST 8 — Test Validation (Should Return 400)
─────────────────────────────────────────

Send bad data — missing required fields:

curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "A",
    "email": "not-an-email",
    "password": "123"
  }'

Expected Response (400 Bad Request):
{
  "timestamp": "2025-06-01T10:00:00Z",
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Request validation failed. Check fieldErrors for details.",
  "fieldErrors": {
    "lastName": "Last name is required",
    "email": "Email must be a valid address",
    "password": "Password must be between 8 and 100 characters",
    "firstName": "First name must be between 2 and 60 characters",
    "role": "Role is required"
  }
}


─────────────────────────────────────────
TEST 9 — Test Rate Limiting
─────────────────────────────────────────

Hit the endpoint more than 60 times per minute to trigger:

Expected Response (429 Too Many Requests):
{
  "timestamp": "...",
  "status": 429,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Max 60 requests/minute. Retry after 60 seconds."
}


─────────────────────────────────────────
TEST 10 — Test RBAC (Should Return 403)
─────────────────────────────────────────

Login as Priya (MEMBER) and try to list all members (ADMIN/TRAINER only):

curl -X GET http://localhost:8080/api/v1/members \
  -H "Authorization: Bearer PRIYA_MEMBER_TOKEN"

Expected Response (403 Forbidden):
{
  "timestamp": "...",
  "status": 403,
  "errorCode": "FORBIDDEN",
  "message": "Insufficient privileges to access this resource"
}


─────────────────────────────────────────
TEST 11 — Test Redis Cache
─────────────────────────────────────────

Step 1: Call the list endpoint (first call hits the database)
  GET /api/v1/subscriptions?status=ACTIVE

Step 2: Call it again immediately (second call hits Redis cache)

How to verify caching is working:
  - Enable SQL logging temporarily: set spring.jpa.show-sql=true
  - First call: you'll see SELECT queries printed in console
  - Second call: NO SQL printed — it came from Redis cache!

Step 3: Check Redis directly to see the cached value:
  redis-cli
  KEYS *                        (list all keys)
  KEYS subscriptionTiers*       (see subscription cache)
  TTL subscriptionTiers::ACTIVE-p0   (see remaining TTL in seconds)


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 3 — DEPLOYMENT STEPS (Render.com)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

─────────────────────────────────────────
STEP 1 — Push to GitHub
─────────────────────────────────────────

  git init
  git add .
  git commit -m "Initial commit: Gym Tracker API"
  git remote add origin https://github.com/YOUR_USERNAME/gym-tracker.git
  git push -u origin main


─────────────────────────────────────────
STEP 2 — Sign Up and Deploy on Render
─────────────────────────────────────────

1. Go to https://render.com and create a free account.

2. In the Render dashboard, click:
   New → Blueprint

3. Connect your GitHub account and select the gym-tracker repo.

4. Render detects render.yaml automatically and shows you 3 services:
   - gym-tracker-api (Web Service — your Spring Boot app)
   - gym-tracker-db  (MySQL Database)
   - gym-tracker-redis (Redis)

5. Click "Apply" — Render provisions everything automatically.

6. The build takes about 3-5 minutes. Once done, your API is live at:
   https://gym-tracker-api.onrender.com


─────────────────────────────────────────
STEP 3 — Environment Variables (Auto-wired)
─────────────────────────────────────────

All variables in render.yaml are auto-injected. You do NOT need
to type them manually. Render connects MySQL and Redis URLs for you.

Only JWT_SECRET is auto-generated by Render (generateValue: true).

If you need a custom JWT secret, go to:
Render Dashboard → gym-tracker-api → Environment → Edit JWT_SECRET


─────────────────────────────────────────
STEP 4 — Verify Deployment
─────────────────────────────────────────

Health check:
  curl https://gym-tracker-api.onrender.com/actuator/health

Swagger UI:
  https://gym-tracker-api.onrender.com/swagger-ui.html


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 4 — UNDERSTANDING REDIS AND RATE LIMITING
         (Beginner Friendly — Zero Prior Knowledge Needed)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

─────────────────────────────────────────
WHAT IS REDIS? (The Simple Explanation)
─────────────────────────────────────────

Think of Redis like this:

  Your application is a restaurant kitchen.
  MySQL is the storeroom in the back — it has everything,
    but it takes time to walk there and get it.
  Redis is the small shelf RIGHT NEXT to the chef —
    it only holds the most-used items, but access is instant.

Redis is an in-memory key-value store.
  - "In-memory" means data lives in RAM, not on disk
  - "Key-value" means: you give it a key → it gives you the value
  - This makes it 100x–1000x faster than a SQL database query

Example of Redis key-value:
  Key: "subscriptionTiers::ACTIVE-p0"
  Value: { [list of 20 subscriptions as JSON] }

When Spring calls @Cacheable, it:
  1. Checks if the key exists in Redis
  2. If YES → return immediately (no database hit!)
  3. If NO  → call the database, save result to Redis, return it

So the first call is slow (database). Every call after that is fast (Redis).


─────────────────────────────────────────
REDIS USE CASE 1 — CACHING (@Cacheable)
─────────────────────────────────────────

In SubscriptionService.java:

  @Cacheable(value = "subscriptionTiers", key = "#status.name() + '-p' + #pageable.pageNumber")
  public Page<SubscriptionResponse> listByStatus(SubscriptionStatus status, Pageable pageable) {
      return subscriptionRepository.findAllByStatus(status, pageable).map(this::toResponse);
  }

What happens line by line:
  1. Admin calls GET /api/v1/subscriptions?status=ACTIVE&page=0
  2. Spring generates the cache key: "ACTIVE-p0"
  3. Spring checks Redis: does "subscriptionTiers::ACTIVE-p0" exist?
  4. First call → Redis is empty → hits MySQL → stores result in Redis
  5. Second call → Redis has the key → returns instantly without touching MySQL
  6. After 1 hour (TTL) → Redis automatically deletes it → next call hits DB again

When admin creates or cancels a subscription:

  @CacheEvict(value = "subscriptionTiers", allEntries = true)
  public SubscriptionResponse cancel(Long id) { ... }

This DELETES all keys in "subscriptionTiers" from Redis.
Why? Because the data changed — the cached list would be stale (wrong).
So we wipe it, and the next read will re-populate from the fresh DB data.


─────────────────────────────────────────
REDIS USE CASE 2 — RATE LIMITING
─────────────────────────────────────────

What is Rate Limiting?
  Imagine a water tap with a flow limiter.
  No matter how hard you turn it, only 60 drops per minute come out.
  Rate limiting is the same concept applied to API requests.

Why do we need it?
  - Prevents one user from sending 10,000 requests per second
  - Protects your server from crashing under excessive load
  - Prevents brute-force attacks (someone trying passwords repeatedly)
  - Makes your API fair for all users


THE TOKEN BUCKET ALGORITHM (How Bucket4j Works)
─────────────────────────────────────────────────

Imagine a bucket that holds 60 tokens.
  - A new token is added every second (60 per minute total)
  - Every API request TAKES one token from the bucket
  - If the bucket is EMPTY → request is REJECTED (429 response)
  - If the bucket HAS tokens → request is ALLOWED

Visual Example:

  Minute starts: [60 tokens in bucket] ████████████████████

  User sends 1 request → [59 tokens]   ███████████████████░
  User sends 1 request → [58 tokens]   ██████████████████░░
  ...
  User sends 60 requests → [0 tokens]  ░░░░░░░░░░░░░░░░░░░░

  User sends 61st request → REJECTED → 429 Too Many Requests
  
  After 1 minute → bucket refills → [60 tokens again]


WHERE THE BUCKET LIVES IN OUR CODE
────────────────────────────────────

In RateLimitFilter.java:

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  private Bucket buildBucket() {
      return Bucket.builder()
          .addLimit(Bandwidth.builder()
              .capacity(60)                           // max 60 tokens
              .refillGreedy(60, Duration.ofMinutes(1)) // refill 60 per minute
              .build())
          .build();
  }

Each user/IP gets their OWN bucket. So if Priya sends 60 requests,
it doesn't affect Uday's bucket. Everyone gets a fair 60 req/min.

KEY IDENTIFICATION:
  - Authenticated user → uses first 40 chars of JWT token as key
  - Anonymous user → uses IP address as key

  private String resolveClientKey(HttpServletRequest request) {
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
          return "user:" + authHeader.substring(7, 47);  // JWT prefix
      }
      return "ip:" + request.getRemoteAddr();  // fallback to IP
  }

WHY JWT AND NOT JUST IP?
  Mobile users change IP addresses as they move between WiFi and 4G.
  Using JWT means the same user has the same bucket regardless of IP.


WHY IS THIS IN REDIS? (Current Implementation Note)
─────────────────────────────────────────────────────

In our current code, buckets are stored in Java memory (ConcurrentHashMap).
This works perfectly for a SINGLE server instance.

For production with MULTIPLE server instances (Render scales horizontally),
you'd migrate to Redis-backed buckets using bucket4j-redis:

  // Production version (Redis-backed, works across multiple pods)
  ProxyManager<String> proxyManager = Bucket4jRedis.casBasedBuilder(redissonClient).build();
  Bucket bucket = proxyManager.builder()
      .addLimit(limit)
      .build(key, configSupplier);

This way, if you have 3 server pods, they all share the SAME bucket state
via Redis, so a user can't bypass the limit by hitting different pods.


HOW THE FILTER WORKS STEP BY STEP
────────────────────────────────────

1. Request arrives at server
2. RateLimitFilter runs BEFORE Spring Security (Order = 1)
3. Extract client identity (JWT or IP)
4. Look up or create a Bucket for that identity
5. Call bucket.tryConsume(1):
   - Returns TRUE  → token taken → continue to next filter → Spring Security → Controller
   - Returns FALSE → bucket empty → return 429 immediately, never reaches controller
6. Add X-RateLimit headers so client knows their remaining quota


─────────────────────────────────────────
REDIS COMMANDS CHEAT SHEET
─────────────────────────────────────────

Connect to Redis:
  redis-cli

See all keys in Redis:
  KEYS *

See all cache keys:
  KEYS subscriptionTiers*
  KEYS workoutTemplates*

Get a specific cached value:
  GET "subscriptionTiers::ACTIVE-p0"

Check how long until a key expires (seconds):
  TTL "subscriptionTiers::ACTIVE-p0"

Delete a key manually:
  DEL "subscriptionTiers::ACTIVE-p0"

Delete all keys (clear entire cache):
  FLUSHALL

Check Redis server info:
  INFO server

Monitor all commands in real-time (useful for debugging):
  MONITOR


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 5 — UNDERSTAND YOUR PROJECT (What Does Each File Do?)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

FLOW OF A REQUEST (End to End)
──────────────────────────────

Browser/Postman → HTTP Request
    ↓
RateLimitFilter         (Is this user sending too many requests?)
    ↓
JwtAuthenticationFilter (Is the JWT token valid? Who is this user?)
    ↓
SecurityConfig          (Is this user ALLOWED to access this URL?)
    ↓
Controller              (Parse the URL, validate the request body)
    ↓
Service                 (Run business logic, check rules)
    ↓
Repository              (Talk to MySQL database)
    ↓
Back to Service         (Map entity to DTO)
    ↓
Back to Controller      (Return ResponseEntity with status code)
    ↓
HTTP Response → Browser/Postman


FILE BY FILE EXPLANATION
──────────────────────────

GymTrackerApplication.java
  The main entry point. The @SpringBootApplication annotation
  tells Spring to scan all classes and wire everything together.
  @EnableCaching activates Redis caching.
  @EnableScheduling activates the daily subscription expiry job.

SecurityConfig.java
  The "bouncer" of your application.
  Defines WHICH endpoints require WHICH roles.
  Sets up JWT filter in the security chain.
  Configures custom 401/403 JSON responses.

JwtTokenProvider.java
  Handles creating and validating JWT tokens.
  Uses HMAC-SHA256 algorithm with a secret key.
  A JWT has 3 parts: Header.Payload.Signature
  The payload contains: username, roles, issued time, expiry time.

JwtAuthenticationFilter.java
  Runs on EVERY request.
  Reads the Authorization header.
  Validates the token and sets the user identity in SecurityContext.
  After this, Spring Security knows WHO is making the request.

RateLimitFilter.java
  Runs BEFORE JWT filter (Order=1).
  Implements token bucket algorithm via Bucket4j.
  Returns 429 if bucket is empty.

GlobalExceptionHandler.java
  Catches ALL exceptions thrown by any service or controller.
  Maps them to clean JSON responses.
  Never lets a stack trace reach the client.

domain/ (JPA Entities)
  These map directly to database tables.
  @Entity → this class = a table.
  @Column → this field = a column.
  @ManyToOne → foreign key relationship.
  @Version → optimistic locking (prevents lost updates).
  NEVER return these from your controllers — use DTOs.

dto/ (Data Transfer Objects)
  request/ → what the client sends to you (with validation annotations)
  response/ → what you send back to the client (clean, no passwords!)

repository/ (Spring Data JPA)
  Just interfaces. Spring generates the SQL implementation.
  findByEmail() → SELECT * FROM users WHERE email = ?
  findAllByStatus() → SELECT * FROM subscriptions WHERE status = ?

service/ (Business Logic)
  This is the BRAIN of the application.
  @Transactional → wraps the method in a DB transaction.
  @Transactional(readOnly=true) → optimization for SELECT-only operations.
  @Cacheable → cache this method's return value in Redis.
  @CacheEvict → delete stale cache entries when data changes.

controller/ (HTTP Layer)
  Handles HTTP routing and nothing else.
  @RestController → combines @Controller + @ResponseBody.
  @RequestMapping → base URL for all endpoints in this controller.
  @Valid → triggers Jakarta validation on the request body.
  @PreAuthorize → method-level role check (second layer of security).
  @PageableDefault → default pagination settings (size=20, sort=createdAt).


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
PART 6 — TOP 20 INTERVIEW QUESTIONS & ANSWERS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

─────────────────────────────────────────
Q1. What is JWT and how does authentication work in your project?
─────────────────────────────────────────

JWT (JSON Web Token) is a compact, self-contained token for
transmitting user identity and claims between parties.

In our project:
1. User calls POST /api/v1/auth/login with email + password
2. AuthService validates credentials via Spring Security's AuthenticationManager
3. JwtTokenProvider creates a signed JWT containing:
   - subject: user's email
   - roles: ["ROLE_ADMIN"]
   - issuedAt: current time
   - expiration: 24 hours later
4. Token is returned to client (stored in localStorage/cookie)
5. On every subsequent request, client sends:
   Authorization: Bearer eyJhbGci...
6. JwtAuthenticationFilter extracts and validates the token
7. If valid → sets Authentication in SecurityContextHolder
8. Spring Security allows the request to proceed

The token is STATELESS — the server stores nothing.
Any server instance can verify any token using the shared secret.


─────────────────────────────────────────
Q2. What is the difference between @Transactional and
    @Transactional(readOnly = true)?
─────────────────────────────────────────

@Transactional (default):
  - Opens a read-write database transaction
  - Hibernate tracks ALL entity changes (dirty checking)
  - Changes are flushed to DB at the end of the method
  - Use for: INSERT, UPDATE, DELETE operations

@Transactional(readOnly = true):
  - Opens a read-only transaction
  - Hibernate SKIPS dirty checking (no need to track changes)
  - DB driver can route query to read replica (in clustered DBs)
  - Slightly faster because less overhead
  - Use for: SELECT-only operations

In our project, we set readOnly=true as the DEFAULT on the service class,
then override with @Transactional on each write method. This follows the
principle of least privilege — be read-only by default.


─────────────────────────────────────────
Q3. Explain RBAC in your project. How do you restrict endpoints?
─────────────────────────────────────────

RBAC = Role-Based Access Control.

We implement it at TWO levels (defense-in-depth):

Layer 1 — URL Pattern Level (SecurityConfig.java):
  .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
  .requestMatchers("/api/v1/members/**").hasAnyRole("ADMIN", "TRAINER")
  This blocks requests at the security filter chain — very fast.

Layer 2 — Method Level (Controllers):
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<SubscriptionResponse> create(...)
  This provides a second check even if someone bypasses URL patterns.

Three roles:
  ROLE_MEMBER  → view own workouts and subscription
  ROLE_TRAINER → manage workout templates + view members
  ROLE_ADMIN   → full system access

If a MEMBER tries to access an ADMIN endpoint:
  1. URL pattern check → blocks with 403 Forbidden
  2. Our custom AccessDeniedHandler returns clean JSON (not Spring's HTML page)


─────────────────────────────────────────
Q4. What is the N+1 problem and how does your project avoid it?
─────────────────────────────────────────

The N+1 problem happens when you load a list of N entities,
then make 1 extra query for each entity to load its relationships.

Example (bad):
  Load 20 subscriptions → 1 query
  For each subscription, load the member → 20 queries
  Total: 21 queries instead of 1!

How we avoid it:
  1. @ManyToOne(fetch = FetchType.LAZY) — don't load relations eagerly
  2. Our DTO mapper accesses only the fields we explicitly need
  3. For the Member's name in SubscriptionResponse, we call
     s.getMember().getFullName() within the same transaction
     (Hibernate loads it in the same session, not a separate query)
  4. In production: use @EntityGraph or JOIN FETCH in the repository
     query for complex screens that need related data.


─────────────────────────────────────────
Q5. What is Optimistic Locking? Why do you use @Version?
─────────────────────────────────────────

Problem: Two admin users open the same subscription simultaneously.
  Admin A reads version=1, changes status to PAUSED
  Admin B reads version=1, changes status to CANCELLED
  Admin A saves → version becomes 2
  Admin B saves → OVERWRITES Admin A's change silently! (Lost update)

@Version solves this:
  Every save includes: WHERE id=? AND version=1
  Admin A saves → WHERE id=1 AND version=1 → SUCCESS → version becomes 2
  Admin B saves → WHERE id=1 AND version=1 → FAILS (version is now 2)
  → Hibernate throws OptimisticLockException
  → We catch it and return a 409 Conflict to Admin B

This prevents data corruption without using database-level locks
(which would block other transactions and reduce throughput).


─────────────────────────────────────────
Q6. Why do you use DTOs instead of returning JPA entities directly?
─────────────────────────────────────────

Three critical reasons:

1. Security:
   Entities contain sensitive fields (password hash, version number,
   internal IDs). Returning them directly leaks this data.
   DTOs let you choose EXACTLY what to expose.

2. LazyInitializationException:
   JPA entities have lazy relationships. If Jackson tries to serialize
   a lazy collection AFTER the transaction closes, it throws an exception.
   DTOs are plain Java objects — no lazy loading issues.

3. API Contract Stability:
   If you rename a column in your entity (DB change), the API response
   would change too, breaking all clients.
   With DTOs, your API contract (field names) is independent of your DB schema.


─────────────────────────────────────────
Q7. How does Redis caching work in your project?
─────────────────────────────────────────

We use Spring's @Cacheable abstraction backed by Redis.

@Cacheable(value = "subscriptionTiers", key = "#status.name() + '-p' + #pageable.pageNumber")
public Page<SubscriptionResponse> listByStatus(SubscriptionStatus status, Pageable pageable)

Flow:
  1. Spring generates cache key: e.g., "subscriptionTiers::ACTIVE-p0"
  2. Checks Redis: does this key exist?
  3. Cache HIT → deserializes JSON from Redis, returns immediately (no DB call)
  4. Cache MISS → runs the method (DB query), serializes result to JSON,
     stores in Redis with TTL=1 hour, returns result

Cache Eviction:
  @CacheEvict(value = "subscriptionTiers", allEntries = true)
  
  When a subscription is created/cancelled/paused, we evict ALL entries
  in the "subscriptionTiers" cache. This ensures clients always see
  fresh data after any mutation.

Different TTLs per cache (configured in RedisConfig.java):
  subscriptionTiers → 1 hour  (changes rarely — admin-only writes)
  workoutTemplates  → 30 min  (changes sometimes — trainer writes)
  default           → 10 min  (for everything else)


─────────────────────────────────────────
Q8. What is the Token Bucket algorithm? How does rate limiting work?
─────────────────────────────────────────

The Token Bucket algorithm:
  - A bucket holds a maximum of N tokens (N = rate limit, e.g., 60)
  - Tokens are added at a fixed rate (60 per minute = 1 per second)
  - Each request consumes 1 token
  - If the bucket has tokens → request allowed, token consumed
  - If the bucket is empty → request rejected with 429

Why Token Bucket over simple counter?
  Simple counter: count 60 per minute, reset at 00:00.
  Problem: a user can send 60 at 00:59 and 60 at 01:00 = 120 in 2 seconds!
  
  Token Bucket: tokens refill CONTINUOUSLY (1 per second).
  This prevents burst attacks while still allowing up to 60 per minute.

In RateLimitFilter.java, each client identity gets its own Bucket.
Bucket4j does all the thread-safe token math.
A ConcurrentHashMap stores buckets keyed by user identity (JWT or IP).


─────────────────────────────────────────
Q9. What is Spring Security Filter Chain?
─────────────────────────────────────────

Spring Security processes every HTTP request through an ordered chain
of filters. Think of it as a series of security checkpoints.

Our filter chain order:
  1. RateLimitFilter (Order=1)        → Are you flooding us?
  2. JwtAuthenticationFilter          → Who are you? (parse JWT)
  3. UsernamePasswordAuthenticationFilter (disabled in our stateless setup)
  4. Authorization checks             → Are you ALLOWED here?

Each filter can:
  - Pass the request to the next filter (chain.doFilter())
  - Short-circuit and return a response immediately (e.g., 429, 401, 403)

The filter chain is configured in SecurityConfig.securityFilterChain().
We use .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
to ensure our JWT filter runs before Spring's default auth filter.


─────────────────────────────────────────
Q10. What is the difference between @RestController and @Controller?
─────────────────────────────────────────

@Controller:
  - Returns a view name (like "index.html" for Thymeleaf/JSP)
  - Used in MVC applications that render HTML pages
  - You need to add @ResponseBody to individual methods to return JSON

@RestController:
  - = @Controller + @ResponseBody on every method
  - Every method automatically serializes return value to JSON
  - Used in REST APIs (which is what we're building)

In our project, all 4 controllers use @RestController because
we always return JSON — never HTML pages.


─────────────────────────────────────────
Q11. What is Pagination and why is it important?
─────────────────────────────────────────

Pagination = returning data in chunks (pages) instead of all at once.

Without pagination:
  GET /api/v1/subscriptions → returns ALL 50,000 subscriptions
  → Massive JSON response
  → Server uses huge memory to build it
  → Network transfer takes minutes
  → Client browser freezes trying to render it

With pagination:
  GET /api/v1/subscriptions?page=0&size=20 → returns 20 subscriptions
  → Tiny response
  → Fast rendering

In Spring Data JPA:
  Page<Subscription> findAllByStatus(SubscriptionStatus status, Pageable pageable)
  
  The Pageable object contains: page number, page size, sort field, sort direction.
  Spring Data generates: SELECT * FROM subscriptions WHERE status=? LIMIT 20 OFFSET 0

The response includes metadata:
  {
    "content": [...],      ← the actual data (20 items)
    "totalElements": 347,  ← total items across all pages
    "totalPages": 18,      ← how many pages total
    "number": 0,           ← current page number
    "size": 20             ← items per page
  }


─────────────────────────────────────────
Q12. Explain the multi-stage Dockerfile. Why is it important?
─────────────────────────────────────────

A multi-stage Dockerfile uses multiple FROM instructions.
Each stage can use a different base image.
Only the final stage is kept in the output image.

Stage 1 (builder): maven:3.9.6-eclipse-temurin-17-alpine
  Purpose: compile Java source code and build the fat JAR
  Contains: Maven, full JDK, build tools
  Size: ~500 MB

Stage 2 (runtime): eclipse-temurin:17-jre-alpine  
  Purpose: run the compiled JAR
  Contains: ONLY the JRE (no Maven, no JDK, no source code)
  Size: ~180 MB

Benefits:
  1. Final image is ~180 MB instead of ~700 MB
  2. Faster deployments (smaller image to pull)
  3. Smaller attack surface (no build tools that could be exploited)
  4. Non-root user (adduser appuser) adds another security layer


─────────────────────────────────────────
Q13. What is @Scheduled and how does the subscription expiry job work?
─────────────────────────────────────────

@Scheduled(cron = "0 0 0 * * *") means: run at midnight every day.
Cron format: second minute hour day-of-month month day-of-week

The expireSubscriptions() method in SubscriptionService:
  1. Queries for all ACTIVE subscriptions where endDate < today
  2. Sets their status to EXPIRED in a batch
  3. Evicts the cache so next reads show correct EXPIRED status
  4. Logs how many were expired

This simulates a real-world billing system where subscriptions
automatically expire when their paid period ends.

@EnableScheduling in GymTrackerApplication enables this feature.


─────────────────────────────────────────
Q14. What happens when validation fails? Walk through the error response.
─────────────────────────────────────────

1. Controller receives: POST /api/v1/auth/register with bad data
2. @Valid triggers Jakarta Bean Validation on RegisterRequest
3. @NotBlank fails on lastName → MethodArgumentNotValidException is thrown
4. Spring looks for an exception handler for this exception type
5. GlobalExceptionHandler.handleValidation() is called
6. We extract all field errors from the BindingResult
7. We build an ApiErrorResponse with fieldErrors map
8. We return ResponseEntity.badRequest() (HTTP 400)

Client receives:
  {
    "timestamp": "2025-06-01T10:30:00Z",
    "status": 400,
    "errorCode": "VALIDATION_FAILED",
    "message": "Request validation failed. Check fieldErrors for details.",
    "fieldErrors": {
      "lastName": "Last name is required",
      "email": "Email must be a valid address"
    }
  }

@JsonInclude(JsonInclude.Include.NON_NULL) on ApiErrorResponse means
fieldErrors only appears in the response when it's not null.
Normal 404 errors won't have a fieldErrors field.


─────────────────────────────────────────
Q15. What is BCrypt and why cost factor 12?
─────────────────────────────────────────

BCrypt is a password hashing algorithm designed to be intentionally SLOW.

Why slow? Because attackers use GPUs to try billions of passwords per second.
If hashing takes 300ms, an attacker can only try ~3 passwords/second instead
of billions. This makes brute-force attacks practically infeasible.

Cost factor (work factor):
  Cost  8 → ~50ms  per hash  (too fast — modern GPUs can handle this)
  Cost 10 → ~100ms per hash  (minimum recommended)
  Cost 12 → ~300ms per hash  (our choice — strong protection, acceptable UX)
  Cost 14 → ~1200ms per hash (too slow for a login endpoint)

BCrypt also automatically:
  - Adds a random salt (prevents rainbow table attacks)
  - Stores the salt inside the hash itself (no separate storage needed)

new BCryptPasswordEncoder(12) sets cost factor to 12.


─────────────────────────────────────────
Q16. What is the difference between authentication and authorization?
─────────────────────────────────────────

Authentication: WHO are you?
  "I am Uday Kumar with email uday@gymtracker.com"
  Verified by: JWT token signature + expiry check
  In code: JwtAuthenticationFilter

Authorization: WHAT are you ALLOWED to do?
  "Uday is ROLE_ADMIN so he can access /api/v1/subscriptions"
  Verified by: role checks
  In code: SecurityConfig URL patterns + @PreAuthorize annotations

Order matters: Authentication always comes FIRST.
You can't check authorization for someone whose identity is unknown.

In Spring Security:
  SecurityContextHolder.getContext().getAuthentication()
  → After authentication: contains the user's principal and authorities
  → Before authentication (or if token invalid): returns AnonymousUser


─────────────────────────────────────────
Q17. What is HikariCP and why configure a connection pool?
─────────────────────────────────────────

A database connection is like a phone call — expensive to establish,
cheap to keep open once established.

Without connection pooling:
  Every request opens a new connection to MySQL → handshake, auth, etc.
  Then closes it when done.
  At 100 concurrent users: 100 connection open/close cycles.
  Each takes ~20-100ms. That's wasted time.

With HikariCP (connection pooling):
  At startup, we pre-open 10 connections to MySQL.
  When a request needs a DB connection → grab one from the pool.
  When done → return it to the pool (don't close it).
  At 100 concurrent users: connections are reused.
  Response time is much faster.

HikariCP is the fastest JDBC connection pool, included by default
in Spring Boot. Our config:
  maximum-pool-size=10  → max 10 simultaneous DB connections
  minimum-idle=2        → always keep 2 connections warm
  connection-timeout=30000 → wait max 30s before throwing an error


─────────────────────────────────────────
Q18. Why use @OneToMany with LAZY fetch instead of EAGER?
─────────────────────────────────────────

EAGER fetch: when you load a Member, also immediately load ALL their
  subscriptions and workout logs from the database.
  
  Problem: Member has 500 workout logs. Loading a member list of 20
  members = 20 members + 10,000 workout logs. You didn't need those!

LAZY fetch (our choice): relationships are NOT loaded until accessed.
  Loading 20 members = just 20 rows.
  If you then call member.getWorkoutLogs() WITHIN the same transaction,
  only THEN does Hibernate fetch the logs for that specific member.

In our project:
  @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
  private List<Subscription> subscriptions = new ArrayList<>();

Since our service methods are @Transactional and our DTOs only access
the specific fields we need (not the lazy collections), we avoid
both the N+1 problem and unnecessary data loading.


─────────────────────────────────────────
Q19. What is the purpose of springdoc-openapi? How does it help?
─────────────────────────────────────────

springdoc-openapi automatically generates API documentation by scanning
your @RestController, @RequestMapping, @RequestBody, etc. annotations.

It generates:
  - /api-docs → machine-readable OpenAPI 3.0 JSON specification
  - /swagger-ui.html → human-readable interactive UI

Benefits:
  1. No manual documentation — it's always in sync with your code
  2. Hiring managers can test your API directly in the browser
  3. Frontend developers use the spec to understand request/response shapes
  4. The "Try it out" button lets anyone test endpoints without Postman

Our annotations make it even better:
  @Tag → groups endpoints into sections (Auth, Members, Subscriptions)
  @Operation → describes what an endpoint does
  @ApiResponse → documents possible HTTP responses
  @Parameter → describes path/query parameters

The OpenApiConfig adds Bearer authentication to the Swagger UI,
so testers can paste a JWT token and test secured endpoints.


─────────────────────────────────────────
Q20. How would you improve this project for a real production environment?
─────────────────────────────────────────

This is a portfolio project. For real production, I would add:

1. Database Migrations (Flyway/Liquibase):
   Replace ddl-auto=update with versioned SQL migration scripts.
   This gives you full control over schema changes and rollback.

2. Redis-backed Rate Limiting:
   Migrate Bucket4j buckets from in-memory to Redis so limits work
   across multiple server instances (horizontal scaling).

3. JWT Token Blacklist:
   Store revoked JWT IDs (JTI) in Redis on logout. The filter checks
   the blacklist before accepting a token. TTL = remaining token lifetime.

4. Refresh Token:
   Short-lived access tokens (15 min) + long-lived refresh tokens (7 days).
   Better security than a single 24-hour token.

5. Distributed Tracing (Micrometer + Zipkin/Jaeger):
   Trace requests across services. Essential for debugging in microservices.

6. API Versioning Strategy:
   /api/v1/ and /api/v2/ running simultaneously during migrations.

7. Circuit Breaker (Resilience4j):
   If Redis goes down, the app should degrade gracefully — skip caching
   but still serve requests (fail open for caching, fail closed for auth).

8. Comprehensive Integration Tests:
   Use @SpringBootTest + Testcontainers (real MySQL + Redis in Docker)
   for full end-to-end testing without mocks.

9. Audit Logging:
   Log who did what and when — critical for a financial system tracking
   subscription payments.

10. Secrets Management:
    Use AWS Secrets Manager or HashiCorp Vault instead of environment
    variables for JWT_SECRET and DB passwords.
