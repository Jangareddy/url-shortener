# Architecture Overview

## 1. System Overview

The prototype contains two primary capabilities:

```text
Client
  |
Spring Boot REST API
  |
  +-----------------------------+
  |                             |
URL Shortener              Workflow API
  |                             |
  |                      WorkflowOrchestrator
  |                             |
  |                       WorkflowGraph
  |                             |
  |                      Engineering Agents
  |                             |
  |                      WorkflowContext
  |
  +---- PostgreSQL
  |
  +---- Redis
```

The URL shortener provides the functional application, while the workflow subsystem demonstrates governed agentic software engineering orchestration.

## 2. URL Shortener Architecture

```text
Client
  |
Controller
  |
Service
  |
  +---- PostgreSQL / JPA
  |
  +---- Redis Cache
```

PostgreSQL stores URL mappings, expiration information and analytics.

Redis reduces database lookups for frequently accessed short URLs.

The service layer handles URL validation, short-code generation, expiration checks and click tracking.

## 3. Agentic Workflow Architecture

The workflow models the software development lifecycle as an explicit dependency graph.

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

This graph supports both sequential and parallel execution.

Testing and review may execute independently after implementation. Documentation acts as a synchronization point and cannot execute until both branches complete.

## 4. Workflow Components

### WorkflowGraph

Creates the workflow tasks and defines dependencies between stages.

### WorkflowOrchestrator

Responsible for:

- Determining which tasks are ready
- Executing engineering agents
- Coordinating parallel paths
- Enforcing approval gates
- Applying policy validation
- Handling retries
- Safe-stop behavior
- Rollback
- Dynamic replanning
- Recording workflow metrics

### WorkflowContext

Maintains state shared across the workflow:

- Workflow identifier
- Start/completion timestamps
- Tasks
- Shared context
- Decisions
- Audit trail

Shared workflow data uses thread-safe collections where parallel execution can occur.

### EngineeringAgent

Provides the abstraction for stage-specific engineering execution.

Each agent supports one workflow stage and produces an engineering output that can be consumed by downstream stages.

## 5. Workflow State Model

Tasks transition between controlled states:

```text
PENDING
   |
RUNNING
   |
COMPLETED
```

Additional governance and failure states include:

```text
WAITING_FOR_APPROVAL
RETRYING
FAILED
ROLLED_BACK
SAFE_STOPPED
```

Only tasks whose dependencies are completed are eligible for execution.

## 6. Human Approval Gates

High-impact stages require explicit human approval:

```text
Requirements
Architecture
Release Readiness
```

When one of these stages is reached, execution stops at:

```text
WAITING_FOR_APPROVAL
```

The workflow continues only after approval is received.

This establishes a controlled-autonomy boundary between automated execution and human oversight.

## 7. Parallel Execution and Synchronization

After implementation completes, testing and review become independently eligible.

```text
             Implementation
              /          \
         Testing        Review
              \          /
              Documentation
```

The orchestrator executes ready tasks asynchronously.

Documentation cannot begin until both testing and review are complete, providing an explicit synchronization point.

## 8. Failure Handling

Task execution uses bounded retries.

```text
Execution Failure
       |
     RETRY
       |
  Retry Limit?
    /      \
   No      Yes
   |        |
PENDING  SAFE_STOPPED
```

A failed task is retried only up to its configured retry limit.

When the retry limit is exceeded, execution transitions to `SAFE_STOPPED` rather than continuing indefinitely.

## 9. Rollback

The workflow exposes an operator-controlled rollback action.

Rollback in this prototype operates on workflow state and generated task output.

It does not automatically reverse external side effects such as:

- Database migrations
- Infrastructure changes
- Production deployments

Those would require integration with deployment and infrastructure systems in a production implementation.

## 10. Dynamic Replanning

When an upstream engineering output changes, dependent downstream tasks are invalidated.

For example:

```text
Architecture Changed
        |
Implementation reset
        |
  +-----+-----+
  |           |
Testing     Review
  |           |
  +-----+-----+
        |
Documentation
        |
Release Readiness
```

Affected tasks return to `PENDING`, previous outputs are cleared, and execution proceeds again according to dependency and approval rules.

This prevents downstream artifacts from remaining valid after their assumptions have changed.

## 11. Context and Decision Lineage

Workflow stages share information through `WorkflowContext`.

The context records:

- Stage outputs
- Scenario information
- Requirement information
- Human decisions
- Replanning decisions
- Retry events
- Rollback events
- Audit events

This provides traceability between upstream decisions and downstream execution.

## 12. Policy Guardrails

Policy validation is performed by the orchestrator before controlled task execution.

Release readiness is treated as a high-impact action and requires human approval.

The policy mechanism is intentionally lightweight for the prototype but provides an extension point for security, compliance and change-management policies.

A production implementation could integrate policy-as-code technologies or enterprise governance services.

## 13. Observability and Metrics

Workflow execution records an audit trail containing significant lifecycle events.

Metrics currently include:
- Completed tasks
- Failed tasks
- Success rate
- Retry count
- Rollback count
- MTTR
- End-to-end duration

These metrics are prototype-level. MTTR is currently approximated from recovered retry execution time and can be made more precise with durable failure and recovery timestamps.

## 14. Scenario Model

The orchestration supports:

### Greenfield

New systems and features.

### Brownfield

Enhancements, refactoring and bug fixes where existing behavior and compatibility must be considered.

### Ambiguous

Requirements containing missing or unclear information where assumptions and human clarification are required.

## 15. Key Design Decisions

### Explicit Graph Instead of Linear Chaining

Dependencies are represented directly, allowing parallel branches and synchronization.

### Separation of Orchestration and Agent Logic

`WorkflowOrchestrator` manages execution while `EngineeringAgent` implementations own stage-specific reasoning.

### Deterministic Agents

Agents are deterministic in the prototype.

This avoids external AI dependencies and makes execution reproducible and testable.

The `EngineeringAgent` abstraction allows LLM-backed implementations to be introduced later.

### Human-Controlled High-Impact Actions

Requirements, architecture and release readiness use explicit approval gates.

### Bounded Autonomy

Retries, approval gates, policy validation and safe-stop behavior limit autonomous execution.

## 16. Production Evolution

A production implementation could extend the architecture with:

- Durable workflow storage
- Distributed workflow engine
- LLM-backed engineering agents
- Repository-aware brownfield analysis
- Policy-as-code integration
- Authentication and authorization
- Distributed tracing
- Expanded reliability metrics
- CI/CD integration
- Deployment rollback automation
- Secrets management

The prototype intentionally keeps these concerns outside its scope to remain locally runnable and reviewable.