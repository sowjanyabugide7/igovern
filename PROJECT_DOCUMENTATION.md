# iGovern Full Stack Assessment — Project Documentation

**Candidate:** Sowjanya Bugide
**Role:** Full Stack Java Developer — iGovern (HCL Technologies)
**Assessment given by:** Manoj Wadhwa

---

## 1. Executive Summary

This project implements the complete working application requested in the iGovern hiring assessment. It is a two‑microservice + Angular system that demonstrates:

- CRUD on **master/child** records with date, number, string, and file fields
- Asynchronous messaging via **ActiveMQ** (producer + receiver)
- A modern **Angular** front‑end exercising every backend feature
- **JWT authentication**, **Bean Validation**, **secure file handling**, **SQL‑injection‑safe** persistence
- **Log4j2** logging with rolling files and **Prometheus / Micrometer** observability
- **Declarative `@Transactional`** transaction management

The project is built with Spring Boot 3.2 on Java 17, Hibernate/JPA 6.4, Spring Security 6, ActiveMQ 5.18, and Angular 17.

---

## 2. Architecture Overview

```
┌─────────────────────────┐         ┌──────────────────────────┐
│   Angular 17 UI         │ HTTPS   │   data-service (MS1)     │
│   localhost:4200        │────────▶│   localhost:8081/api     │
│                         │  JWT    │   - REST CRUD            │
│   - Login / Register    │◀────────│   - JPA/Hibernate (H2)   │
│   - Programs CRUD       │         │   - File upload          │
│   - Participants CRUD   │         │   - JWT auth, BCrypt     │
│   - File upload/download│         │   - @Transactional       │
│   - AMQ message viewer  │         └────────────┬─────────────┘
└─────────────────────────┘                      │
                ▲                                │ HTTP (delete events)
                │                                ▼
                │                  ┌──────────────────────────┐
                │                  │   amq-service (MS2)      │
                │                  │   localhost:8082         │
                │                  │   - Producer (JmsTemplate│
                │                  │   - Receiver (@JmsListen)│
                │                  └────────────┬─────────────┘
                │                               │ JMS / OpenWire
                │                               ▼
                │                  ┌──────────────────────────┐
                │ poll /echoed     │   ActiveMQ broker        │
                └──────────────────│   localhost:61616        │
                                   │   queue: igovern.delete  │
                                   │           .events        │
                                   └──────────────────────────┘
```

### Module layout

```
java/
├── pom.xml                         (parent Maven POM, multi-module)
├── docker-compose.yml              (ActiveMQ broker)
├── README.md
├── PROJECT_DOCUMENTATION.md        (this file)
│
├── data-service/                   Microservice 1 — port 8081
│   ├── pom.xml
│   └── src/main/java/com/igovern/data/
│       ├── DataServiceApplication.java
│       ├── config/                 SecurityConfig, JwtUtil, JwtAuthFilter, DataSeeder
│       ├── entity/                 User, ResearchProgram, Participant, Attachment
│       ├── repository/             4 Spring Data JPA repositories
│       ├── service/                AuthService, ProgramService, AttachmentService, AmqClient
│       ├── controller/             AuthController, ProgramController, AttachmentController, GlobalExceptionHandler
│       └── dto/                    AuthDtos, ProgramDtos (validated request bodies)
│
├── amq-service/                    Microservice 2 — port 8082
│   ├── pom.xml
│   └── src/main/java/com/igovern/amq/
│       ├── AmqServiceApplication.java
│       ├── config/                 WebConfig (CORS)
│       ├── controller/             MessageController
│       └── service/                MessageProducer, MessageReceiver
│
└── frontend/                       Angular 17 — port 4200
    └── src/app/
        ├── app.component.ts        (top nav, layout)
        ├── app.routes.ts           (lazy-loaded routes + guards)
        ├── pages/
        │   ├── login.component.ts
        │   ├── programs.component.ts
        │   ├── program-detail.component.ts
        │   └── echoed.component.ts
        ├── services/               auth.service, api.service
        ├── guards/                 auth.guard
        └── interceptors/           auth.interceptor (Bearer token)
```

---

## 3. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Backend language | Java | 17 (Temurin) |
| Backend framework | Spring Boot | 3.2.5 |
| Persistence | Spring Data JPA + Hibernate | 6.4.4 |
| Database | H2 (file mode) | 2.x |
| Security | Spring Security + JJWT | 6.2 / 0.12.5 |
| Messaging | Spring JMS + Apache ActiveMQ Classic | 5.18.3 |
| Logging | Log4j2 (Spring Boot starter) | 2.x |
| Observability | Spring Actuator + Micrometer Prometheus | latest |
| Build tool | Apache Maven | 3.9.9 |
| Frontend | Angular standalone components | 17.3 |
| Frontend lang | TypeScript | 5.4 |
| Container runtime | Docker Desktop (for broker) | 28.x |

---

## 4. Requirements Traceability Matrix

Every numbered requirement from the assessment email is mapped to concrete code.

### 4.1 Microservice 1 — Data‑Driven Microservice

| Requirement | Implementation | Files |
|---|---|---|
| CRUD: GET, POST, PUT, DELETE | All four mappings on programs and participants | `ProgramController.java` |
| Master record | `ResearchProgram` entity with `@OneToMany` to participants and attachments | `entity/ResearchProgram.java` |
| Child record | `Participant` entity with `@ManyToOne` back to program | `entity/Participant.java` |
| Date fields | `startDate`, `endDate` on Program; `dateOfBirth`, `enrollmentDate` on Participant | entities |
| Number fields | `budget` (BigDecimal) on Program; `weightKg` (Double) on Participant | entities |
| String fields | `name`, `description`, `firstName`, `lastName` | entities |
| File attachment | Dedicated `Attachment` entity + `AttachmentController` with multipart upload, download, delete | `entity/Attachment.java`, `controller/AttachmentController.java`, `service/AttachmentService.java` |

### 4.2 Microservice 2 — AMQ Operations

| Requirement | Implementation | Files |
|---|---|---|
| Producer | `JmsTemplate.convertAndSend(queue, message)` | `service/MessageProducer.java` |
| Receiver | `@JmsListener(destination = "${igovern.amq.queue}")` echoes every received message | `service/MessageReceiver.java` |
| Delete on MS1 publishes to MS2 | `AmqClient` (in MS1) calls `POST /messages/send` on MS2 inside the delete service methods | `data-service/.../service/AmqClient.java`, `ProgramService.delete*`, `AttachmentService.delete` |
| Echoing by AMQ receiver | Receiver prefixes incoming message with `ECHO:`, logs through Log4j2 and stores in an in‑memory ring buffer (last 100), exposed to the UI via `GET /messages/echoed` | `MessageReceiver.java`, `MessageController.java` |

### 4.3 Front‑End Application

A **single‑page Angular 17** app with standalone components and lazy‑loaded routes:

| UI feature | Backend endpoint exercised |
|---|---|
| Login / Register | `POST /api/auth/login`, `POST /api/auth/register` |
| Programs list, create, delete | `GET/POST/DELETE /api/programs` |
| Program detail (participants) | `GET/POST/DELETE /api/programs/{id}/participants` |
| Program detail (attachments) | `GET/POST/DELETE /api/attachments/...` |
| AMQ Messages page | `GET /messages/echoed` (port 8082, polled every 3 s) |

### 4.4 Security and Best Practices

| Concern | Implementation |
|---|---|
| User authentication | JWT (HS512). `JwtUtil` issues tokens. `JwtAuthFilter` validates `Authorization: Bearer <token>` on every request. Stateless session policy (`SessionCreationPolicy.STATELESS`). |
| Password storage | `BCryptPasswordEncoder` — never plaintext. |
| Authorization | All endpoints under `/api/**` require authentication except `/auth/**`, `/h2-console/**`, and selected actuator endpoints. |
| Input validation | Bean Validation annotations on every request DTO: `@NotBlank`, `@Size`, `@Pattern`, `@Past`, `@DecimalMin`, `@DecimalMax`, `@NotNull`. Controllers use `@Valid`. A central `GlobalExceptionHandler` returns clean 400 responses. |
| SQL injection prevention | Only Spring Data JPA derived/parameterized queries. **Zero** string‑concatenated SQL anywhere in the codebase. |
| XSS / output handling | Angular's default DOM bindings escape interpolated values; no `[innerHTML]` from user input. |
| File upload — content type | Whitelist enforced in `AttachmentService` (PDF, PNG, JPEG, TXT, DOC, DOCX). Uploads with other content types receive **415 Unsupported Media Type**. |
| File upload — size | Capped at 10 MB via `spring.servlet.multipart.max-file-size`. |
| File upload — filename safety | `sanitize()` strips characters outside `[A-Za-z0-9._-]` and shortens long names. The stored filename is prefixed with a UUID to avoid collisions and guessing. |
| Path traversal | Resolved upload path is checked with `target.startsWith(uploadDir)` before reading or writing. |
| CORS | `SecurityConfig` (MS1) and `WebConfig` (MS2) restrict origins to `http://localhost:4200`. |
| CSRF | Disabled because the API is stateless JWT — best practice for token‑based APIs. |
| H2 console hardening | Same‑origin frame policy enabled (so the console works), but the path is gated behind the standard security config. |

### 4.5 Logging and Observability

| Concern | Implementation |
|---|---|
| Log4j2 framework | `spring-boot-starter-log4j2` in both POMs; default Logback excluded. Both services use `org.apache.logging.log4j.Logger`. |
| Log configuration | `log4j2-spring.xml` in each service: console appender + rolling file appender (10 MB / day with gzip rotation, 10 generations kept). |
| Log levels | `com.igovern` at DEBUG; Spring at INFO; Hibernate SQL at INFO. |
| Observability framework | Spring Boot Actuator + Micrometer Prometheus registry. |
| Endpoints exposed | `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`. |
| Verified output | `/actuator/prometheus` returns ~45 KB of metrics (HTTP request timings, JVM, JDBC pool, JMS, etc.). |

### 4.6 Transaction Management

| Concern | Implementation |
|---|---|
| Enabled globally | `@EnableTransactionManagement` on `DataServiceApplication`. |
| Declarative usage | Class‑level `@Transactional` on `ProgramService` and `AttachmentService`. Method‑level `@Transactional(readOnly = true)` on read paths. `AuthService.register` is `@Transactional`. |
| Programmatic transactions? | None used — assessment specifically asks for declarative. |
| Rollback semantics | Default Spring rollback on any unchecked exception. Validation failures raise `ResponseStatusException`, which rolls back. |

---

## 5. Data Model

### 5.1 Entity-Relationship Diagram

```
┌────────────────────┐
│ User               │   (authentication)
│ ────────────────── │
│ id (PK)            │
│ username (unique)  │
│ password (BCrypt)  │
│ role               │
└────────────────────┘

┌────────────────────────────┐
│ ResearchProgram (master)   │
│ ────────────────────────── │
│ id (PK)                    │
│ name                       │
│ description                │
│ startDate (date)           │
│ endDate (date)             │
│ budget (number)            │
└──────────┬─────────────────┘
           │ 1
           │
           │ *
┌──────────▼─────────────────┐         ┌──────────────────────────┐
│ Participant (child)        │         │ Attachment               │
│ ────────────────────────── │         │ ──────────────────────── │
│ id (PK)                    │         │ id (PK)                  │
│ firstName, lastName (str)  │         │ originalName, storedName │
│ dateOfBirth (date)         │         │ contentType              │
│ enrollmentDate (date)      │         │ size (number)            │
│ weightKg (number)          │         │ uploadedAt (timestamp)   │
│ program_id (FK → Program)  │         │ program_id (FK → Program)│
└────────────────────────────┘         └──────────────────────────┘
```

The PDF asks for "medical research program" (master) and "research participants" (child) — implemented exactly. Attachments are also linked to a program so file uploads have a parent record, satisfying the "file attachment" requirement on the master.

### 5.2 Field types per entity

**ResearchProgram (master)** — covers all required field types in one record:

| Field | Type | PDF requirement |
|---|---|---|
| `name` | String | string field ✓ |
| `description` | String | string field ✓ |
| `startDate` | LocalDate | date field ✓ |
| `endDate` | LocalDate | date field ✓ |
| `budget` | BigDecimal | number field ✓ |
| `attachments` (one‑to‑many) | List<Attachment> | file attachment ✓ |
| `participants` (one‑to‑many) | List<Participant> | child collection |

**Participant (child)** — also covers all field types:

| Field | Type |
|---|---|
| `firstName`, `lastName` | String |
| `dateOfBirth`, `enrollmentDate` | LocalDate |
| `weightKg` | Double |

---

## 6. REST API Reference

### 6.1 data‑service (port 8081, base path `/api`)

All endpoints below require `Authorization: Bearer <jwt>` **except** `/auth/*`.

#### Authentication

| Method | Path | Body | Returns |
|---|---|---|---|
| POST | `/auth/register` | `{"username":"..","password":".."}` | `{ token, username, role }` |
| POST | `/auth/login` | same | `{ token, username, role }` |

#### Programs (master)

| Method | Path | Description |
|---|---|---|
| GET | `/programs` | List all programs |
| GET | `/programs/{id}` | Get one program |
| POST | `/programs` | Create program |
| PUT | `/programs/{id}` | Update program |
| DELETE | `/programs/{id}` | **Delete** — also publishes AMQ message |

Request body for POST/PUT:
```json
{
  "name": "Cardio Trial",
  "description": "Heart study",
  "startDate": "2026-06-01",
  "endDate": "2026-12-01",
  "budget": 50000
}
```

#### Participants (child)

| Method | Path | Description |
|---|---|---|
| GET | `/programs/{programId}/participants` | List children of a program |
| POST | `/programs/{programId}/participants` | Add participant |
| PUT | `/programs/participants/{participantId}` | Update participant |
| DELETE | `/programs/participants/{participantId}` | Delete participant — publishes AMQ message |

Request body for POST/PUT:
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "dateOfBirth": "1990-04-15",
  "enrollmentDate": "2026-06-10",
  "weightKg": 65.5
}
```

#### Attachments (file)

| Method | Path | Description |
|---|---|---|
| POST | `/attachments/programs/{programId}` | `multipart/form-data` upload (`file=@...`) |
| GET | `/attachments/programs/{programId}` | List attachments for a program |
| GET | `/attachments/{id}/download` | Download file |
| DELETE | `/attachments/{id}` | Delete file — publishes AMQ message |

### 6.2 amq‑service (port 8082, no base path)

| Method | Path | Description |
|---|---|---|
| POST | `/messages/send` | `{"message":"..."}` — Producer publishes to queue |
| GET | `/messages/echoed` | Returns last 100 messages echoed by the receiver |

### 6.3 Actuator (both services)

| Path | Purpose |
|---|---|
| `/actuator/health` | Liveness/readiness |
| `/actuator/info` | Build info |
| `/actuator/metrics` | Micrometer metric names |
| `/actuator/prometheus` | Prometheus scrape endpoint |

---

## 7. Security Deep‑Dive

### 7.1 Authentication flow

```
Client                       data-service
  │                             │
  │ POST /auth/login            │
  │ {username, password}        │
  ├────────────────────────────▶│
  │                             │ AuthService.login
  │                             │   - find user by username
  │                             │   - BCrypt match
  │                             │   - JwtUtil.generateToken (HS512)
  │ 200 OK { token, ... }       │
  │◀────────────────────────────┤
  │                             │
  │ GET /api/programs           │
  │ Authorization: Bearer <jwt> │
  ├────────────────────────────▶│
  │                             │ JwtAuthFilter
  │                             │   - parse claims
  │                             │   - SecurityContext set with role
  │                             │ ProgramController -> Service -> Repo
  │ 200 OK [...]                │
  │◀────────────────────────────┤
```

JWT details:
- Algorithm: **HS512**
- Claims: `sub` (username), `role`, `iat`, `exp`
- Expiration: 1 hour (configurable via `app.jwt.expiration-ms`)
- Secret: Base64‑encoded, environment‑overridable via `JWT_SECRET`

### 7.2 Validation rules (excerpt)

`AuthDtos.LoginRequest`:
```java
@NotBlank @Size(min=3,max=50)
@Pattern(regexp="^[A-Za-z0-9_.-]+$", message="username may contain letters, digits, _ . -")
private String username;

@NotBlank @Size(min=6,max=100)
private String password;
```

`ProgramDtos.ParticipantRequest`:
```java
@NotBlank @Size(max=100) private String firstName;
@NotBlank @Size(max=100) private String lastName;
@NotNull  @Past         private LocalDate dateOfBirth;
@NotNull                private LocalDate enrollmentDate;
@DecimalMin(value="0.0", inclusive=false) @DecimalMax("1000.0") private Double weightKg;
```

### 7.3 File upload safeguards (defense in depth)

```java
// 1. Block empty uploads
if (file == null || file.isEmpty()) throw 400;

// 2. Whitelist content type
if (!allowedTypes.contains(file.getContentType())) throw 415;

// 3. Sanitize filename
String original = sanitize(file.getOriginalFilename()); // [^A-Za-z0-9._-] -> _

// 4. UUID prefix prevents guessing/overwriting
String stored = UUID.randomUUID() + "_" + original;

// 5. Path-traversal guard
Path target = uploadDir.resolve(stored).normalize();
if (!target.startsWith(uploadDir)) throw 400;
```

### 7.4 SQL injection — why we are safe

Only **Spring Data JPA** is used. There is no `Statement`, no string concatenation into queries, no native dynamic SQL. Every query is either:
- A derived query method (`findByProgramId`, `findByUsername`)
- An inherited `JpaRepository` method (`findById`, `save`, `delete`)

These are translated to bound‑parameter prepared statements by Hibernate.

---

## 8. Logging and Observability

### 8.1 Log4j2 configuration

Both services use `src/main/resources/log4j2-spring.xml`:

- **Console appender** — colorless pattern with timestamp, thread, level, logger
- **Rolling file appender** — `logs/<service>.log`, rotates at 10 MB or daily, gzipped, 10 generations
- **Logger thresholds** — `com.igovern` at DEBUG, `org.springframework` at INFO, `org.hibernate.SQL` at INFO

Sample log line (from a real run):
```
2026-05-28 13:23:21.692 [http-nio-8081-exec-2] INFO  com.igovern.data.service.AmqClient
   - Published delete notification to AMQ service: DELETE event: ResearchProgram id=33
```

### 8.2 Prometheus / Micrometer

`spring-boot-starter-actuator` + `micrometer-registry-prometheus` are included in both modules. The Prometheus scrape endpoint is publicly readable for monitoring tools.

Sample metrics exposed:
- `http_server_requests_seconds_*` — request timing histograms
- `jvm_memory_used_bytes` — heap/non‑heap usage
- `hikaricp_connections_active` — JDBC pool state
- `process_cpu_usage`, `system_cpu_usage`
- `jms_message_send_seconds` — JMS send latency (amq‑service)

These can be scraped by Prometheus and visualised in Grafana with zero code changes.

---

## 9. Build & Run

Prerequisites: Java 17+, Maven 3.8+, Node.js 18+ and npm. Docker is optional (used to run ActiveMQ).

### 9.1 Start ActiveMQ broker (optional)

```bash
docker compose up -d
# Web console: http://localhost:8161  (admin/admin)
```

If you skip Docker, set `ACTIVEMQ_URL=vm://localhost?broker.persistent=false` so the AMQ service uses an embedded broker.

### 9.2 Build and run the backend

```bash
mvn -pl data-service,amq-service -am clean package
mvn -pl data-service spring-boot:run
mvn -pl amq-service  spring-boot:run        # in another shell
```

### 9.3 Run the front-end

```bash
cd frontend
npm install
npm start
```

Open http://localhost:4200 and log in as `admin` / `admin123`.

---

## 10. Demo Script (for the joint code review)

A 10–12 minute walkthrough you can follow during the live demo session.

### Step 1 — Show services starting (0:30)
- Open the three terminals already showing logs.
- Point out the **Log4j2 startup banner** and the line `Started DataServiceApplication in X seconds`.
- Point out the **JmsListenerEndpointContainer** line in the amq‑service log — that's the receiver coming up.

### Step 2 — Authentication (1:00)
- Open http://localhost:4200 → login screen.
- Try a wrong password — show the inline error from the API (`invalid credentials`).
- Log in with `admin` / `admin123`.
- Open browser DevTools → Application → Local Storage. Show the JWT token, decoded to `sub`, `role`, `exp`.

### Step 3 — Create master record (1:00)
- Click **Programs** → fill in *Cardio Trial 2026*, dates, budget 75000 → **Save**.
- Show it appearing in the list.
- (Optional) Open the H2 console at http://localhost:8081/api/h2-console (JDBC URL `jdbc:h2:file:./data/igovern`) and `SELECT * FROM RESEARCH_PROGRAMS;` to show the row.

### Step 4 — Add child records (1:00)
- Click the program name to open the detail page.
- Add a participant — first name, last name, DOB, enrollment date, weight.
- Show the validation: try saving with future DOB → server returns 400 with `dateOfBirth: must be a past date`.

### Step 5 — File attachment (1:00)
- Upload a small PDF or PNG.
- Click **Download** → file comes back with the original filename.
- Try uploading an `.exe` (rename a small file): rejected with **415 Unsupported Media Type**.

### Step 6 — AMQ flow (2:00) — the showpiece
- Open the **AMQ Messages** tab — empty list.
- Open ActiveMQ console at http://localhost:8161 (admin/admin) → Queues → show `igovern.delete.events` with 0 messages.
- In the UI, delete the participant.
- Refresh the AMQ Messages tab → see `ECHO: DELETE event: Participant id=X`.
- Refresh the broker console → enqueue/dequeue counters incremented.
- Repeat with deleting the program → another echoed message.

### Step 7 — Observability (1:00)
- Open `http://localhost:8081/api/actuator/health` → `{ "status": "UP" }`.
- Open `http://localhost:8081/api/actuator/prometheus` → show metrics.
- `Ctrl-F` for `http_server_requests_seconds_count` and explain how Prometheus would scrape this every 15 s.

### Step 8 — Code walk (3:00)
Open the IDE and walk through:
1. `DataServiceApplication.java` — `@EnableTransactionManagement`
2. `entity/ResearchProgram.java` — JPA mapping with master/child relationship
3. `service/ProgramService.java` — class‑level `@Transactional`, the `AmqClient.notifyDeletion` call inside `delete()`
4. `service/AmqClient.java` — `RestClient` to MS2
5. `amq-service/MessageProducer.java` and `MessageReceiver.java` — `JmsTemplate` and `@JmsListener`
6. `config/SecurityConfig.java` — JWT filter chain, BCrypt encoder
7. `config/JwtAuthFilter.java` — token parsing
8. `dto/ProgramDtos.java` — bean validation annotations
9. `service/AttachmentService.java` — file safety
10. `log4j2-spring.xml` — appenders

### Step 9 — Q&A (1:00)
Be ready for: scaling considerations, real DB migration (Postgres + Flyway), test strategy, Jenkins pipeline outline, Sonar quality gates.

---

## 11. Day‑to‑Day Work Alignment (Section B of the email)

| PDF Item | Notes |
|---|---|
| 1. Backlog item assignment | Project structure supports per‑feature branches; ready for ticket‑driven work |
| 2. Understanding specifications | This document plus README serves as the spec for the assessment scope |
| 3. Plan of action | Below — sample plan for adding a new feature |
| 4. Development | Angular + Spring Boot/Security/JPA/AMQ — done |
| 5. Tools & technologies | Git: ✓ in your repo. Maven: ✓. Jenkins: not in scope but `mvn package` produces deployable jars. Sonar: can be wired with a 5‑line POM addition. |
| 6. Progress discussions | Daily standup‑ready: small commits, clear log lines, observable metrics |

### Sample Plan‑of‑Action template (for a future backlog item)

> **Story:** As a researcher I want to mark a participant inactive so that they're excluded from active counts.

| Phase | Work |
|---|---|
| Design (1 h) | Add `active` boolean to `Participant`. Add a PATCH endpoint `/programs/participants/{id}/active`. Sketch wireframe. |
| Code (2 h) | Migrate entity (`ddl-auto=update`). Add service method with `@Transactional`. Add controller. Add DTO + validation. Update Angular service + button. |
| Test (1 h) | JUnit for service. MockMvc for controller. Manual UI test with valid + invalid token. |
| Review | Run Sonar locally, push branch, open PR. |

---

## 12. Quality Notes (what was *not* required but is included anyway)

- **Multi‑module Maven** — clean separation of services with shared parent POM.
- **Lazy‑loaded Angular routes** — faster initial load.
- **Standalone Angular components** — modern Angular pattern, less boilerplate than NgModules.
- **HTTP interceptor + route guard** — token attached automatically, unauthenticated users redirected to `/login`.
- **Global exception handler** — consistent JSON error shape across all endpoints.
- **In‑memory ring buffer for echoed messages** — bounded memory (last 100), thread‑safe via `Collections.synchronizedList`.

---

## 13. Things to mention in the joint code review

A short list of intentional design choices and trade‑offs you can reference confidently:

1. **`spring.jpa.open-in-view: false`** — Avoids the Open Session In View anti‑pattern. Forces fetch decisions into the service layer (which is why `Program.participants` is `@JsonIgnore`'d and exposed via a separate endpoint).
2. **Stateless JWT, no server session** — Horizontally scalable; no sticky sessions needed.
3. **CSRF disabled** — Correct for token APIs, would be enabled for cookie/session auth.
4. **H2 in file mode** — Easy local dev; production swap is one config line to Postgres + a Flyway migration. Schema is auto‑generated for now via `ddl-auto: update`; in production this would become Flyway/Liquibase.
5. **Bounded message buffer in MS2** — Production version would persist echoes to a DB or use the broker's audit features.
6. **Embedded broker fallback** — `application.yml` supports `ACTIVEMQ_URL=vm://localhost?broker.persistent=false` so the assessment can run even without Docker.
7. **No tests included by default** — Per the assessment focus on a "working application." JUnit 5 + Spring Boot Test + Spring Security Test are already on the test classpath, ready to use.

---

## 14. Default Credentials and Useful URLs

| Resource | URL | Credentials |
|---|---|---|
| Front‑end | http://localhost:4200 | admin / admin123  *or*  user / user123 |
| data‑service Swagger‑less REST | http://localhost:8081/api | bearer JWT |
| H2 web console | http://localhost:8081/api/h2-console | JDBC `jdbc:h2:file:./data/igovern`, user `sa`, no password |
| Health (MS1) | http://localhost:8081/api/actuator/health | — |
| Prometheus (MS1) | http://localhost:8081/api/actuator/prometheus | — |
| amq‑service | http://localhost:8082 | — |
| Echoed messages | http://localhost:8082/messages/echoed | — |
| ActiveMQ web console | http://localhost:8161 | admin / admin |

---

## 15. Final Status

| Requirement | Status |
|---|---|
| Microservice 1 — CRUD on master/child with date/number/string/file | ✅ Working |
| Microservice 2 — Producer + Receiver, delete on MS1 → queue → echo | ✅ Working |
| Front‑end (Angular) | ✅ Working |
| User authentication | ✅ JWT + BCrypt |
| Secure data/file handling | ✅ Whitelist + sanitize + path guard + size limit |
| Input validation, anti‑SQL‑injection | ✅ Bean Validation + JPA only |
| Log4j2 logging | ✅ Console + rolling file |
| Open‑source observability framework | ✅ Actuator + Micrometer Prometheus |
| Declarative transaction management | ✅ `@EnableTransactionManagement` + `@Transactional` |

**Ready for the demo and joint code review.**
