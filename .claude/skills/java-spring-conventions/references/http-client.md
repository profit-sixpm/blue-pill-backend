# HTTP Client with Spring RestClient

## RestClient Overview

### Purpose

RestClient is Spring's modern HTTP client for:
- Making HTTP requests to external APIs
- Blocking I/O operations
- Type-safe request/response handling
- Error handling and retries

### Why RestClient

**Modern Approach**:
- Replaces deprecated RestTemplate
- Cleaner, more flexible API
- Better error handling
- Supports virtual threads

**Use Cases**:
- Calling external REST APIs
- Third-party integrations
- Microservice-to-microservice communication

## RestClient Configuration

### Creating RestClient Instance

**Dependency Injection**:
- Spring provides RestClient.Builder
- Inject into service class
- Build configured client

**Configuration**:
- Base URL for API
- Default headers (authentication, content-type)
- Timeout settings
- Error handling strategies

## Making Requests

### Code Example - RestClient Usage

**GET Request with Error Handling**:
```java
/**
 * 외부 API를 호출하는 서비스.
 */
@Service
public class ExternalProductService {
    private final RestClient restClient;

    /**
     * 상품 정보를 외부 API에서 조회합니다.
     *
     * @param productId 조회할 상품 ID
     * @return 상품 정보, 없으면 null
     */
    public @Nullable ProductDto getProduct(long productId) {
        try {
            return restClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .body(ProductDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        }
    }
}
```

**POST Request**:
```java
/**
 * 결제 처리 서비스.
 */
@Service
public class PaymentService {
    private final RestClient restClient;

    /**
     * 결제를 처리합니다.
     *
     * @param orderId 주문 ID
     * @param amount 결제 금액
     * @throws PaymentException when payment service is unavailable
     */
    public void processPayment(long orderId, long amount) {
        try {
            restClient.post()
                .uri("/payments")
                .body(new PaymentRequest(orderId, amount))
                .retrieve()
                .body(PaymentResponse.class);
        } catch (HttpServerErrorException ex) {
            throw new PaymentException("Payment service unavailable", ex);
        }
    }
}
```

### GET Request

**Retrieve Data**:
- Fetch single resource
- Fetch list of resources
- Query parameters for filtering

**Response Handling**:
- Deserialize to object
- Handle not found (404)
- Handle errors

### POST Request

**Create Resource**:
- Send request body
- Include authentication headers
- Handle response (created object)

**Request Body**:
- Send data structure (record or object)
- ContentType: application/json
- Serialization automatic

### PUT/PATCH Request

**Update Resource**:
- PUT: Replace entire resource
- PATCH: Partial update
- Send updated data
- Handle response

### DELETE Request

**Remove Resource**:
- Delete by ID
- No request body
- Handle success response

## Error Handling

### Error Responses

**HTTP Error Codes**:
- 4xx: Client errors (bad request, not found, unauthorized)
- 5xx: Server errors (internal error, service unavailable)

**Error Handling Strategies**:
- Throw custom exception
- Return error response to caller
- Log error for debugging
- Retry on transient failures

### Exception Handling

**Custom Exceptions**:
- Create domain-specific exceptions
- Extend from application exception
- Include error details (status, message)

**Error Propagation**:
- Service layer handles API errors
- Transform to domain exceptions
- Let caller decide how to handle

## Timeout Configuration

### Timeout Settings

**Connection Timeout**:
- Time to establish connection
- Prevent hanging indefinitely
- Set reasonable default (5-10 seconds)

**Read Timeout**:
- Time to read response
- Prevent slow responses blocking
- Set based on API SLA

**Configuration**:
- Set when building RestClient
- May be override per request

## Virtual Threads Integration

### Blocking I/O with Virtual Threads

**Use Virtual Threads For**:
- RestClient calls (blocking I/O)
- Database queries (blocking I/O)
- File I/O operations

**Benefits**:
- Thousands of concurrent threads
- Simple synchronous code
- Efficient resource usage

**Configuration**:
- Spring Boot 3.2+ auto-configures
- Set spring.threads.virtual.enabled=true
- Lightweight threading model

## Request/Response Serialization

### Request Serialization

**Automatic JSON Conversion**:
- Record or object automatically serialized
- Uses configured ObjectMapper
- ContentType: application/json

**Custom Serialization**:
- Override ObjectMapper
- Custom JSON formatting
- Custom type handling

### Response Deserialization

**Automatic JSON Conversion**:
- JSON response mapped to object
- Type-safe response handling
- Null safety validation

**Error Response**:
- Deserialize error response
- Extract error details
- Create meaningful exception

## Testing HTTP Calls

### Mocking External Calls

**Unit Testing**:
- Mock RestClient
- Return canned responses
- Verify correct calls made

**Integration Testing**:
- Use WireMock or MockServer
- Real HTTP calls to test server
- Verify request/response

### Test Patterns

**Arrange-Act-Assert**:
- Mock the external API
- Call service method
- Verify response handling

**Error Scenarios**:
- Test timeout handling
- Test error response handling
- Test retry logic

## Common Patterns

### Wrapping External API

**Service Layer Pattern**:
- Service calls RestClient
- Transforms external response to domain objects
- Error handling at service level
- Infrastructure layer for API client

**Repository Pattern**:
- Use RestClient to fetch data
- Treat external API like data source
- Repository interface abstracts HTTP calls

### Retry Logic

**Automatic Retries**:
- Retry on transient failures (500, 503, timeout)
- Exponential backoff
- Maximum retry attempts

**Manual Retry**:
- Loop with sleep between retries
- Break on success or max attempts
- Log each retry attempt

### Caching Responses

**Cache External Data**:
- Cache frequently accessed data
- TTL based expiration
- Reduce external API calls

**Cache Invalidation**:
- When to invalidate cache
- Background refresh strategy
- Handle stale data
