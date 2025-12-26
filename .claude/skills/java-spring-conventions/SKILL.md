---
name: java-spring-conventions
description: Java and Spring Framework development conventions and architectural guidelines. Use when Backend Worker needs to implement code following project standards including layered architecture, Domain-Driven Design, type safety, JPA guidelines, and validation patterns.
---

# Java/Spring Conventions

This skill provides comprehensive conventions for Java and Spring development used by Backend Worker.

## Architecture Overview

The project follows strict **Layered Architecture** with three distinct layers:

1. **Presentation Layer**: Controllers, request/response handling
2. **Service Layer**: Business logic orchestration, domain logic composition
3. **Infrastructure Layer**: Data access, external integrations

**Dependency Rule**: Dependencies flow downward only (Presentation → Service → Infrastructure)
- Never skip layers or create reverse dependencies
- Each layer may only depend on the layer directly below it

## Core Design Principles

**Domain-Driven Design (DDD)**:
- Use rich domain models with embedded business logic
- Place domain logic in domain model classes
- Service layer orchestrates and composes domain logic
- Keep business rules close to the data they operate on

**Type Safety**:
- Use primitive types for non-nullable fields
- Use wrapper types only for nullable fields
- Apply JSpecify annotations for null checking
- Make nullability explicit at type level

**Immutability**:
- Use record classes for DTOs
- Prefer immutable data structures
- Avoid mutable shared state

## Reference Documents

See references/ directory for detailed guidelines:

- **architecture.md**: Layered architecture patterns and responsibilities
- **type-safety.md**: Primitive vs wrapper types, null safety with JSpecify
- **immutability.md**: Record classes and immutable patterns
- **jpa-guidelines.md**: JPA entity design with BaseEntity pattern, domain logic placement
- **validation.md**: Jakarta Validation usage and patterns
- **database.md**: Flyway migrations and schema management
- **http-client.md**: RestClient usage for HTTP calls
- **concurrency.md**: Virtual threads, pinning avoidance, blocking I/O
- **api-documentation.md**: API documentation with SpringDoc OpenAPI annotations
- **testing.md**: Unit tests, integration tests, Given-When-Then pattern

## Implementation Notes for Backend Worker

When implementing code:
1. Reference the appropriate guide from references/
2. Follow naming conventions consistently
3. Ensure dependencies flow correctly
4. Keep layers separated with no crosscutting
5. Use type safety to prevent null pointer exceptions
6. Validate input at system boundaries

## Project Structure

Typical package structure follows layers:
- com.project.presentation.controller
- com.project.service
- com.project.domain
- com.project.infrastructure.repository
- com.project.infrastructure.external

Each layer has clear responsibilities and dependencies.
