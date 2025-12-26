# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a housing subscription (주택청약) system built for a hackathon. The system helps users analyze their eligibility for Korean housing subscription notices using LLM-powered parsing and RAG-based consulting.

**Critical Architecture Principle**: LLMs are NOT used for eligibility decisions. Application logic performs all numerical comparisons and eligibility judgments. LLMs only extract structured data from PDFs and generate natural language consulting summaries.

## Technology Stack

- **Language**: Java 21 with Virtual Threads enabled
- **Framework**: Spring Boot 3.5.9
- **Build Tool**: Gradle (Kotlin DSL)
- **Database**: PostgreSQL (H2 for development)
- **Migration**: Flyway
- **AI/ML**: Spring AI 1.1.2 with OpenAI integration, PGVector for embeddings
- **Security**: Spring Security with stateless sessions
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **External APIs**: Upstage Document Parser, LH (Korea Land & Housing Corporation) API

## Common Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application (requires environment variables)
./gradlew bootRun

# Clean build artifacts
./gradlew clean

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "ClassName"

# Run a single test method
./gradlew test --tests "ClassName.methodName"
```

### Database
- Database configuration uses environment variables (see application.properties)
- Flyway manages schema migrations (located in `src/main/resources/db/migration`)
- JPA is configured with `ddl-auto=validate` - schema changes must go through Flyway migrations

## Environment Variables

Required environment variables (see `application.properties`):
- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `OPENAI_API_KEY`: OpenAI API key for LLM
- `OPENAI_CHAT_MODEL`: OpenAI model identifier
- `OPENAI_CHAT_COMPLETIONS_PATH`: Custom completions path
- `OPENAI_BASE_URL`: OpenAI base URL
- `UPSTAGE_API_KEY`: Upstage API key for PDF parsing
- `LH_API_SERVICE_KEY`: Korea Land & Housing Corporation API key

## Architecture

### Package Structure

The project follows a layered architecture with clear separation of concerns:

```
com.sixpm/
├── application/        # Application layer (use cases, business logic)
├── common/            # Shared utilities and cross-cutting concerns
├── config/            # Configuration classes
│   └── security/      # Security configuration
├── infrastructure/    # External integrations (APIs, databases, vector stores)
└── presentation/      # REST controllers and DTOs
    └── health/        # Health check endpoint
```

### Layered Architecture Principles

1. **Presentation Layer** (`presentation/`)
   - REST controllers with OpenAPI documentation
   - Request/response DTOs only
   - No business logic
   - Use `@Tag` and `@Operation` annotations for API documentation

2. **Application Layer** (`application/`)
   - Use cases and business logic
   - Orchestrates domain logic and infrastructure
   - **Performs all eligibility decisions using application code** (not LLM)
   - Numerical comparisons, requirement validation, scoring calculations

3. **Infrastructure Layer** (`infrastructure/`)
   - External API clients (Upstage, LH API, OpenAI)
   - Vector store operations (PGVector)
   - Repository implementations

4. **Common Layer** (`common/`)
   - Shared utilities, constants, exceptions
   - Cross-cutting concerns

### LLM Usage Guidelines

**LLMs DO**:
- Parse PDF documents and extract structured data
- Generate natural language consulting summaries
- Summarize eligibility reasons for users

**LLMs DO NOT**:
- Make eligibility decisions (pass/fail)
- Perform numerical comparisons
- Validate requirements
- Calculate scores or points

All decision logic must be implemented as deterministic application code in the `application/` layer.

### Data Flow

1. **Notice Collection**: PDF → Upstage Parser → LLM extracts structured data → PostgreSQL + PGVector
2. **User Analysis**: User profile → Application logic decision → Pass/fail result
3. **Consulting**: RAG retrieves relevant documents → LLM generates natural language summary

## Security Configuration

- CORS enabled for `http://localhost:3000` and `https://localhost:3000`
- Stateless session management
- Public endpoints: `/health`, `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/api/v1/auth/**`
- All other endpoints require authentication

## API Documentation

Swagger UI available at: `http://localhost:8080/swagger-ui.html` (when running)

## Development Practices

### JPA and Database
- Use `@Entity` with proper JPA annotations
- Repositories extend `JpaRepository`
- `open-in-view=false` is set - avoid lazy loading outside transactions
- Flyway migrations required for schema changes

### Null Safety
- Project uses JSpecify for null safety annotations
- Prefer non-null types, explicitly mark nullable fields

### Logging
- DEBUG level for `com.sixpm` package
- SQL logging enabled for development

### Testing
- Use JUnit 5 (JUnit Platform)
- Spring Boot Test for integration tests
- Security tests with Spring Security Test

## Key Implementation Areas

Based on the hackathon guide (docs/task.md and README.md), the system implements:

1. **Notice Collection & Parsing**: Automated collection from LH API, PDF parsing with Upstage, structured data extraction with LLM
2. **Natural Language Search**: Vector similarity search using embeddings in PGVector
3. **Notice Details**: Detailed information retrieval from PostgreSQL
4. **Eligibility Determination**: Application logic-based decision making (critical: no LLM involvement)
5. **Consulting Generation**: RAG-based context retrieval + LLM summarization
6. **Caching**: SHA-256 hash-based PDF parsing cache to prevent redundant operations

## External API Documentation

- **LH API**: OpenAPI guides available in `docs/` directory (Korean)
- **Upstage**: Document Parser for PDF processing
- **OpenAI**: Through Spring AI integration

## MCP Servers

Context7 MCP server is configured in `.mcp.json` for enhanced documentation access.
