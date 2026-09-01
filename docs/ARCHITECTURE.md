# Architecture Overview

## Application Architecture

```text
Client
  |
Spring Boot REST API
  |
  +-----------------------+
  |                       |
URL Shortener        Workflow API
  |                       |
  |                 WorkflowOrchestrator
  |                       |
  |                 Workflow Dependency Graph
  |                       |
  |                 Engineering Agents
  |
  +---- PostgreSQL
  |
  +---- Redis