# Type Safety and Null Safety Guide

## Primitive vs Wrapper Types

### When to Use Primitive Types
**Use for non-nullable, required fields**:
- int, long, boolean, double, float, byte, short, char
- No null value possible
- More efficient memory usage
- Clearly indicates field is required

### When to Use Wrapper Types
**Use for nullable, optional fields**:
- Integer, Long, Boolean, Double, Float, Byte, Short, Character
- Can be null
- Explicitly indicates field is optional
- Use only when null is meaningful

### Examples

**Non-nullable fields** (primitive types):
- User ID in system
- Age of person (always present)
- Count of items
- Product price
- Boolean flags with default values

**Nullable fields** (wrapper types):
- Optional middle name
- Phone number (might not be provided)
- Optional discount percentage
- Conditional fields

### Code Example - Type Selection

**Order Entity with Type Safety**:
```java
public class Order {
    private long orderId;           // Required, primitive
    private long userId;            // Required, primitive
    private long totalPrice;        // Required, primitive
    private int itemCount;          // Required, primitive
    private boolean isConfirmed;    // Required, primitive

    private @Nullable String notes; // Optional, wrapper (nullable)
    private @Nullable Long couponId; // Optional, wrapper (nullable)
    private @Nullable Double discountPercent; // Optional, wrapper (nullable)
}
```

**Request DTO with Type Safety**:
```java
public record CreateOrderRequest(
    @NotEmpty
    List<Long> productIds,          // Required list

    @Min(1)
    int quantity,                   // Required quantity (primitive)

    @Nullable
    String deliveryNotes            // Optional notes (can be null)
) { }
```

**Service Method with Type Safety**:
```java
public class OrderService {
    // Returns primitive - order ID always exists
    public long createOrder(List<Long> productIds, int quantity) {
        Order order = new Order(generateId(), productIds, quantity);
        return order.getOrderId();
    }

    // Returns nullable - order might not exist
    public @Nullable Order findOrderById(long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    // Parameter with primitive - required
    public void confirmOrder(long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException());
        order.confirm();
    }
}
```

## JSpecify Annotations

### Purpose
Make null contracts explicit in code and enable static analysis.

### Key Annotations
- @Nullable: Field/parameter can be null
- @NonNull: Field/parameter must not be null
- Applied to classes, methods, fields, parameters

### Usage

**On Class Fields**:
```
- Non-nullable field: private String name;  // implicitly @NonNull
- Nullable field: private @Nullable String middleName;
```

**On Method Parameters**:
```
- Required parameter: public void processOrder(Order order)
- Optional parameter: public void updateName(@Nullable String name)
```

**On Method Return**:
```
- Always returns value: public String getName()
- Might return null: public @Nullable String findUserById(long id)
```

### Validation

The combination guides static analysis and runtime validation:
- Primitive types: Cannot be null by definition
- Non-nullable wrapper: Runtime check should ensure non-null
- Nullable wrapper: Code must handle null possibility

## Nullability Contract

### Making Null Contracts Clear

**Clear Non-Nullable**:
- Primitive type (int, long, etc.)
- Final non-nullable reference with @NonNull

**Clear Nullable**:
- Wrapper type that could be null
- Annotated with @Nullable

### Benefits

1. **Static Analysis**: Tools detect null misuse at compile time
2. **Documentation**: Code shows intent regarding nullability
3. **IDE Support**: Better autocomplete and warnings
4. **Prevents Bugs**: Null pointer exceptions become compile-time issues

## Guidelines

**Rule**: No null surprises
- If something can be null, document it with @Nullable
- If something can't be null, use primitive type or @NonNull
- Trust the type system to enforce contracts

**Choosing Between Types**:
1. Field is always present? → Primitive type
2. Field might not exist? → Wrapper type with @Nullable
3. Field is required? → Primitive or @NonNull
4. Field is optional? → @Nullable wrapper type

## Common Patterns

**Builder Pattern Validation**:
- Set primitive types to default (0, false, etc.)
- Set wrapper types to null for optional fields
- Validate required fields before building

**Optional Pattern** (Java 8+):
- Alternative to nullable wrappers
- Optional<T> explicitly wraps optional values
- Use Optional for method returns, not fields

**Default Values**:
- Primitive types have default values (0, false)
- Wrapper types default to null
- Set explicitly to avoid confusion
