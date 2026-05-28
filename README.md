# iGovern Full Stack Assessment

A two-microservice + Angular application built per the iGovern hiring assessment.

## What's inside

| Module | Tech | Port | Purpose |
|--------|------|------|---------|
| `data-service` | Spring Boot 3, Spring Data JPA, Spring Security, Log4j2, Micrometer/Prometheus, H2 | **8081** | CRUD on **ResearchProgram** (master) and **Participant** (child) records, plus file attachments. JWT auth. |
| `amq-service`  | Spring Boot 3, Spring JMS, ActiveMQ, Log4j2 | **8082** | Producer + receiver. When data-service deletes a record, it calls this service which queues the message; a JMS listener consumes and **echoes** it. |
| `frontend`     | Angular 17 (standalone components) | **4200** | UI for login, CRUD on programs/participants, file upload/download, and viewing echoed AMQ messages. |

## Assessment requirements coverage

- **Microservice 1: Data-Driven** – CRUD on master/child records (`ResearchProgram` ↔ `Participant`) with date, number, string fields and file attachments.
- **Microservice 2: AMQ Operations** – Both producer (`MessageProducer`) and receiver (`MessageReceiver`); deleting a record on MS1 publishes a message that the receiver echoes.
- **Front-End** – Angular 17 application that triggers every backend feature.
- **Security** – JWT-based authentication, BCrypt password hashing, bean-validation on every input, file-type whitelist, filename sanitization, path-traversal protection. SQL injection avoided by using only JPA/Spring Data parameterized queries.
- **Logging & Observability** – Log4j2 (console + rolling file). `spring-boot-starter-actuator` + Micrometer/Prometheus exposes `/actuator/prometheus` for any open-source monitor (Prometheus + Grafana).
- **Transaction Management** – `@EnableTransactionManagement` plus declarative `@Transactional` on all write paths in services.

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- Docker (optional – for ActiveMQ broker)

## Build and run

### 1. Start ActiveMQ broker (optional but recommended)

```bash
docker compose up -d
# Web console: http://localhost:8161  (admin/admin)
```

If you skip Docker, set `ACTIVEMQ_URL=vm://localhost?broker.persistent=false` so the AMQ service uses an embedded broker.

### 2. Build & run the backend

```bash
mvn -pl data-service,amq-service -am clean package
mvn -pl data-service spring-boot:run
mvn -pl amq-service  spring-boot:run    # in another terminal
```

### 3. Run the front-end

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`. Default users:

- `admin` / `admin123`
- `user`  / `user123`

## API Quick Reference

All data-service endpoints are under `http://localhost:8081/api` and require `Authorization: Bearer <jwt>` (except `/auth/*`).

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/register` | Create user, returns JWT |
| POST | `/auth/login` | Returns JWT |
| GET | `/programs` | List programs |
| POST | `/programs` | Create program |
| GET | `/programs/{id}` | Get program |
| PUT | `/programs/{id}` | Update program |
| DELETE | `/programs/{id}` | Delete program (publishes AMQ message) |
| GET | `/programs/{id}/participants` | List child participants |
| POST | `/programs/{id}/participants` | Add participant |
| DELETE | `/programs/participants/{id}` | Delete participant (publishes AMQ message) |
| POST | `/attachments/programs/{id}` | Upload file (multipart) |
| GET | `/attachments/programs/{id}` | List attachments |
| GET | `/attachments/{id}/download` | Download file |
| DELETE | `/attachments/{id}` | Delete attachment (publishes AMQ message) |

amq-service (`http://localhost:8082`):

| Method | Path | Description |
|--------|------|-------------|
| POST | `/messages/send` | Producer – body: `{ "message": "..." }` |
| GET | `/messages/echoed` | Recent messages echoed by the receiver |

## Observability

- Health: `http://localhost:8081/actuator/health`, `http://localhost:8082/actuator/health`
- Prometheus scrape: `/actuator/prometheus` on each service
- Logs roll to `logs/data-service-*.log` and `logs/amq-service-*.log`

## H2 Console (dev only)

`http://localhost:8081/api/h2-console` – JDBC URL `jdbc:h2:file:./data/igovern`, user `sa`, no password.
