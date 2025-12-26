# Jakarta Validation Guide

## Validation Scope

### Where to Validate

**Validation Boundary** (Presentation Layer):
- Validate all external input
- Use Jakarta Validation annotations on request DTOs
- Reject invalid data at system boundary

**Trust Internal Code**:
- No validation between layers
- Internal services and repositories trusted
- Assume data already validated

## Common Validation Annotations

### String Validation

**@NotNull**: Value cannot be null
- Used on required fields
- Works with any type

**@NotBlank**: String cannot be null or whitespace
- String specific
- Trims whitespace before checking
- More strict than NotEmpty

**@NotEmpty**: Collection/String cannot be empty
- Works with collections and strings
- Can be empty after trimming spaces
- Less strict than NotBlank for strings

**@Size**: Collection or string size constraints
- min: minimum size/length
- max: maximum size/length
- Applied to String or Collection

**@Pattern**: String matches regex pattern
- Pattern parameter: regular expression
- For email, phone, custom formats

### Numeric Validation

**@Min**: Minimum numeric value
- Applied to numeric types
- Value >= specified minimum

**@Max**: Maximum numeric value
- Applied to numeric types
- Value <= specified maximum

**@Positive**: Value must be positive
- Applied to numeric types
- Value > 0

**@Negative**: Value must be negative
- Applied to numeric types
- Value < 0

### Email and URL

**@Email**: Valid email address format
- Uses standard email pattern
- Can be customized with regexp

**@URL**: Valid URL format (optional, custom validation)
- Validates proper URL structure

## Request DTO Structure

### Validation on Request Records

**Requirements**:
- Apply annotations to record fields
- @NotNull on required fields
- @Nullable for optional fields (if using JSpecify)
- Size/pattern constraints as needed

### Code Example - Request Validation

**Request DTO with Validation**:
```java
/**
 * 주문 생성 요청 DTO.
 * 모든 필드 검증은 이 레코드에서 수행됩니다.
 */
public record CreateOrderRequest(
    @NotEmpty(message = "Product IDs cannot be empty")
    List<Long> productIds,

    @Min(value = 1)
    int quantity,

    @Nullable
    String deliveryNotes
) { }
```

**Controller with @Valid**:
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * 주문을 생성합니다.
     * 요청 DTO의 검증이 실패하면 400 Bad Request를 반환합니다.
     */
    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return new OrderResponse(order.getId(), order.getTotalPrice());
    }
}
```

### Custom Validation

**@Validated**:
- Enable on controller class
- Triggers validation of @Valid parameters

**@Valid**:
- Applied to controller method parameter
- Triggers cascade validation on nested objects
- Rejects request if validation fails

## Validation Groups

### Conditional Validation

**Use Cases**:
- Different validation for create vs update
- Different rules for different scenarios
- Reuse same DTO with different rules

**Implementation**:
- Define validation groups (interfaces)
- Apply groups to constraints
- Specify groups in validation trigger

## Error Handling

### Validation Error Response

**Standard Pattern**:
- Global exception handler catches validation errors
- Returns structured error response
- Lists all validation violations

**Error Response Format**:
- Field name that failed
- Violation message
- Rejected value
- Constraint name

### Custom Error Messages

**Message Parameter**:
- Override default validation message
- Make messages user-friendly
- Use message interpolation

**Examples**:
- @NotBlank(message = "Product name is required")
- @Size(min=3, max=50, message = "Name must be 3-50 characters")

## Best Practices

1. **Validate at Boundary**: Request validation only
2. **Trust Internal Code**: No validation between layers
3. **Clear Messages**: Error messages help client understand issue
4. **Reasonable Constraints**: Don't over-validate
5. **Fail Fast**: Reject invalid input immediately
6. **Document Rules**: Comment on why constraints exist

## Common Patterns

### Required vs Optional

**Required Field**:
- Apply @NotNull or @NotBlank
- Never null in DTO

**Optional Field**:
- No @NotNull annotation
- Can be null
- Handle null in service layer

### Range Validation

**Numeric Range**:
- @Min for lower bound
- @Max for upper bound
- Both together for range

### String Patterns

**Email Address**:
- @Email annotation
- Or custom @Pattern with email regex

**Format Requirements**:
- @Pattern with specific regex
- Document pattern meaning
- Provide examples

### Collection Validation

**Non-Empty Collection**:
- @NotEmpty on List/Set field
- @Valid on elements if nested objects
- Size constraints if needed

## Integration with Spring

### Automatic Validation

**Spring Boot Integration**:
- Validation trigger automatic
- Invalid requests return 400
- Error message in response

**Customization**:
- Global exception handler for custom format
- Validation provider configuration
- Custom validation implementation
