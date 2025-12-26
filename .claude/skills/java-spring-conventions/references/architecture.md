# Layered Architecture Guide

## Three-Layer Structure

### Presentation Layer
**Responsibility**: Handle HTTP requests and responses

**Contains**:
- Controller classes
- Request DTOs/records
- Response DTOs/records
- Exception handlers
- Validation annotations on request objects

**Dependencies**:
- Depends on Service Layer only
- Never directly depends on Infrastructure Layer
- Never contains business logic

**Naming Convention**:
- Class naming: *Controller
- Package: presentation.controller

### Service Layer
**Responsibility**: Business logic orchestration and domain logic composition

**Contains**:
- Service classes
- Application orchestration logic
- Domain logic composition
- Transactions management

**Dependencies**:
- Depends on Infrastructure Layer only
- Uses Domain models
- Calls repository methods
- Calls external services

**Naming Convention**:
- Class naming: *Service
- Package: service

**Key Pattern**:
- Orchestrates domain objects
- Doesn't duplicate domain logic
- Composes multiple domain operations
- Manages transactions

### Infrastructure Layer
**Responsibility**: Data access and external integration

**Contains**:
- Repository interfaces and implementations
- JPA entities
- External API clients
- Database utilities
- Caching implementations

**Dependencies**:
- No dependencies on other layers
- Uses Spring Data JPA
- Manages database connections
- Calls external APIs

**Naming Convention**:
- Repository: *Repository
- Entity: *Entity
- Package: infrastructure.repository, infrastructure.external

## Dependency Flow

```
Presentation Layer
    ↓ depends on
Service Layer
    ↓ depends on
Infrastructure Layer
```

**Never Reverse**: Infrastructure layer never calls Service layer.

## Cross-Cutting Concerns

**Validation**:
- Request validation in Presentation layer
- Use Jakarta Validation annotations on request DTOs

**Exception Handling**:
- Global exception handler in Presentation layer
- Domain exceptions in domain models
- Transform to HTTP responses in Presentation

**Transactions**:
- Manage in Service layer
- Use @Transactional on service methods
- Infrastructure layer handles data persistence

**Logging**:
- Log at appropriate layer
- Controller logs requests/responses
- Service logs business operations
- Repository logs database operations

## Communication Between Layers

**Presentation → Service**:
- Passes request DTO
- Receives response DTO or domain object

**Service → Infrastructure**:
- Calls repository methods
- Receives domain entities or data objects
- Calls external clients

**Domain Models**:
- Can be used throughout layers
- Represent core business concepts
- Contain business logic
- Immutable when possible

## Common Mistakes to Avoid

**Reverse Dependencies**: Service calling Controller
**Layer Skipping**: Controller calling Repository directly
**Business Logic in Persistence**: Domain logic in entity setters
**Thick Controllers**: Complex logic in Controller methods
**Thin Services**: Service just forwarding to Repository without orchestration
