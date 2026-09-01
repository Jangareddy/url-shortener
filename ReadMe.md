# Agentic URL Shortener

A Spring Boot URL shortener extended with a governed agentic software engineering workflow.

The prototype demonstrates how a requirement can move through the software development lifecycle using explicit workflow dependencies, agent execution, human approval gates, validation, auditability, retries, rollback, replanning, and release-readiness controls.

---

## 1. Technology Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Redis
- Docker Compose
- Maven
- JUnit 5
- Mockito

---

## 2. URL Shortener Features

The application supports:

- Creating short URLs
- Redirecting short URLs to original URLs
- PostgreSQL persistence
- Redis caching
- Optional URL expiration
- Click analytics
- Last-access timestamp
- URL validation
- Collision-safe short-code generation
- Bounded generation retries

### Create Short URL

```http
POST /api/v1/urls