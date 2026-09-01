# Agentic URL Shortener

A Spring Boot URL shortener extended with a governed agentic software engineering workflow.

This prototype demonstrates how a software requirement can move through the SDLC using explicit workflow dependencies, engineering agents, human approval gates, validation, auditability, retries, rollback, replanning, and release-readiness controls.

## Technology Stack

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

## URL Shortener Features

The application supports:

- Creating short URLs
- Redirecting short URLs to original URLs
- PostgreSQL persistence
- Redis caching
- Optional URL expiration
- Click analytics
- Last-access timestamp
- URL validation
- Short-code collision checks
- Bounded short-code generation retries

### Create Short URL

```http
POST /api/v1/urls
```

Example:

```json
{
  "originalUrl": "https://www.example.com",
  "expirationDays": 30
}
```

### Get URL Metadata

```http
GET /api/v1/urls/{shortCode}
```

### Redirect

```http
GET /{shortCode}
```

Returns an HTTP redirect to the original URL.

---

# Agentic Engineering Workflow

The second part of the prototype implements a governed software engineering workflow.

A requirement progresses through:

```text
Requirements
     |
Architecture
     |
Implementation
   /          \
Testing      Review
   \          /
  Documentation
       |
Release Readiness
```

Testing and review can execute independently after implementation. Documentation waits for both paths to complete before release readiness begins.

## Workflow Stages

The workflow contains:

1. Requirement analysis
2. Architecture/design
3. Implementation planning
4. Testing
5. Engineering review
6. Documentation
7. Release readiness

Each stage is represented by a `WorkflowTask` with:

- Dependencies
- Execution status
- Retry count
- Human approval requirement
- Agent output
- Failure reason
- Start/completion timestamps

## Engineering Agents

Each workflow stage is handled through the `EngineeringAgent` abstraction.

Implemented agents include:

- `RequirementAgent`
- `ArchitectureAgent`
- `ImplementationAgent`
- `TestingAgent`
- `ReviewAgent`
- `DocumentationAgent`
- `ReleaseReadinessAgent`

The prototype intentionally uses deterministic local agents rather than requiring an external LLM API key.

This keeps the prototype reproducible and testable while providing an extension point where an LLM-backed implementation could later replace or augment individual agents.

## Controlled Autonomy

Agents execute work within explicit workflow boundaries.

High-impact stages require human approval:

- Requirements
- Architecture
- Release readiness

When an approval-controlled stage is reached, the workflow enters:

```text
WAITING_FOR_APPROVAL
```

Execution continues only after approval is provided.

## Stateful Orchestration

Tasks can transition through states including:

```text
PENDING
RUNNING
WAITING_FOR_APPROVAL
COMPLETED
FAILED
RETRYING
ROLLED_BACK
SAFE_STOPPED
```

The orchestrator executes only tasks whose dependencies have completed successfully.

This allows the workflow to support sequential execution, parallel branches, synchronization points, retries, approval gates and replanning.

## Parallel Execution

After implementation completes:

```text
Testing
Review
```

can execute in parallel.

Documentation waits until both have completed.

This demonstrates a non-linear dependency graph rather than simple sequential task chaining.

## Retry and Safe Stop

Failed tasks are retried using a bounded retry count.

After the retry limit is exceeded, the task transitions to:

```text
SAFE_STOPPED
```

This prevents uncontrolled execution loops and leaves the workflow in a reviewable state.

## Rollback

An operator can mark a workflow task as rolled back.

Rollback in this prototype is orchestration-level rollback. It resets the selected workflow artifact/state; it does not attempt to reverse an external database migration, infrastructure deployment or production change.

Affected downstream work can subsequently be replanned.

## Dynamic Replanning

When an upstream output changes, the workflow can invalidate tasks that depend on it.

For example:

```text
Architecture changes
        |
Implementation invalidated
        |
Testing + Review invalidated
        |
Documentation invalidated
        |
Release Readiness invalidated
```

The workflow then executes the affected path again while preserving governance and approval gates.

## Auditability

`WorkflowContext` maintains:

- Workflow start/completion timestamps
- Task status
- Agent outputs
- Decisions
- Approval decisions
- Retry activity
- Rollback activity
- Replanning activity
- Audit trail

This provides traceability across workflow stages.

---

# Supported Scenarios

The workflow supports three assessment scenarios.

## Greenfield

Used for a new system or feature.

The requirement agent decomposes the requirement into:

- Functional requirements
- API requirements
- Persistence concerns
- Reliability considerations
- Testing expectations
- Release considerations

## Brownfield

Used for an enhancement, refactor or bug fix in an existing system.

The workflow considers:

- Existing behavior
- Impacted modules
- API compatibility
- Persistence/data impact
- Regression risk
- Minimal-impact changes
- Validation requirements

The prototype reasons from the requirement and supplied system context. It does not dynamically clone and inspect arbitrary external repositories.

## Ambiguous Requirement

Used when a requirement lacks sufficient detail.

The workflow identifies:

- Missing information
- Assumptions
- Areas requiring clarification
- Risks caused by ambiguity

Human approval provides a governance checkpoint before implementation continues.

---

# Workflow API

### Start Workflow

```http
POST /api/v1/workflows
```

Example:

```json
{
  "scenarioType": "GREENFIELD",
  "requirement": "Create a URL shortener with expiration and click analytics"
}
```

Supported scenario types:

```text
GREENFIELD
BROWNFIELD
AMBIGUOUS
```

### Get Workflow

```http
GET /api/v1/workflows/{workflowId}
```

### Approve Task

```http
POST /api/v1/workflows/{workflowId}/tasks/{taskId}/approve
```

### Roll Back Task

```http
POST /api/v1/workflows/{workflowId}/tasks/{taskId}/rollback
```

### Replan From Task

```http
POST /api/v1/workflows/{workflowId}/tasks/{taskId}/replan
```

### Workflow Metrics

```http
GET /api/v1/workflows/{workflowId}/metrics
```

Current metrics include:

- Completed tasks
- Failed tasks
- Success rate
- Retry count
- Rollback count
- MTTR
- End-to-end workflow duration

---

# Running Locally

## Prerequisites

Install:

- Java 17+
- Docker
- Maven, or use the included Maven wrapper

## Start PostgreSQL and Redis

```bash
docker compose up -d
```

## Run Tests

Using Maven:

```bash
mvn clean test
```

or the Maven wrapper:

```bash
./mvnw clean test
```

## Start the Application

```bash
./mvnw spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

Health information is available through Spring Boot Actuator.

---

# Testing Approach

The test suite covers important application and orchestration behavior including:

- URL validation
- Workflow dependency graph
- Requirement-agent scenarios
- Human approval gates
- Workflow progression
- Dynamic replanning
- Invalid approval attempts

The design keeps orchestration logic separated from individual agents so both can be tested independently.

---

# Validation and Risk Controls

The prototype includes several controls intended to keep autonomous execution bounded:

- Explicit task dependencies
- Human approval checkpoints
- Policy validation
- Bounded retries
- Safe-stop behavior
- Rollback capability
- Dynamic replanning
- Audit logging
- URL validation
- Database uniqueness constraints
- Dependency synchronization

Release readiness is treated as a high-impact stage and requires human approval.

---

# Architecture Decisions

Key design decisions include:

### Explicit Dependency Graph

Workflow ordering is represented through task dependencies instead of hard-coded sequential chaining.

### Agent Abstraction

`EngineeringAgent` separates orchestration from stage-specific execution.

### Human Approval Gates

High-impact decisions remain under human control.

### Bounded Failure Handling

Retries have explicit limits and transition to a safe-stop state rather than retrying indefinitely.

### Stateless Application APIs / Stateful Workflow Prototype

The URL-shortener data is persisted in PostgreSQL, while workflow execution state is maintained by an in-memory workflow store for prototype simplicity.

A production implementation would persist workflow state in durable storage.

---

# Assumptions and Limitations

This is a prototype built to demonstrate engineering orchestration concepts.

Current limitations include:

- Workflow state is stored in memory and is lost when the application restarts.
- Engineering agents are deterministic local implementations rather than external LLM-backed agents.
- Brownfield analysis reasons from supplied requirements/context rather than dynamically inspecting arbitrary repositories.
- Rollback represents workflow-level rollback and does not reverse external database or deployment changes.
- Reliability metrics are prototype-level. Success rate and MTTR are included; MTTR is approximated from recovered retry execution time and can be made more precise with durable failure and recovery timestamps.
- Parallel workflow execution uses application-process asynchronous execution rather than a distributed workflow engine.
- Policy enforcement demonstrates the governance pattern but is not a complete enterprise security/compliance policy engine.

---

# Production Evolution

For a production implementation, the prototype could be extended with:

- Durable workflow persistence
- Distributed workflow execution
- LLM-backed engineering agents
- Repository-aware brownfield analysis
- Stronger policy-as-code controls
- Authentication and authorization
- Expanded observability and metrics
- CI/CD integration
- Deployment rollback integration
- Secrets management

These capabilities are intentionally outside the prototype scope.

---

# Engineering Summary

The solution combines a working URL-shortener service with a governed agentic SDLC workflow.

The design emphasizes:

- Explicit task decomposition
- Dependency-aware orchestration
- Controlled agent autonomy
- Human oversight
- Parallel execution
- Validation
- Failure containment
- Auditability
- Dynamic replanning
- Maintainable Spring Boot design

The primary trade-off is keeping the prototype locally runnable and deterministic rather than introducing external AI services or distributed workflow infrastructure.

The architecture is designed so those capabilities can be introduced without replacing the core orchestration model.

For additional architecture details, see:

```text
docs/ARCHITECTURE.md
```