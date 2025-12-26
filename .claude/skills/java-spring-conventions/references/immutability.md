# Immutability Patterns Guide

## Record Classes for DTOs

### What are Records?

Java record classes provide:
- Automatic constructor generation
- Automatic equals() and hashCode()
- Automatic toString()
- Final, immutable by default
- Concise syntax for data carriers

### When to Use Records

**Use for DTOs**:
- Request objects from API
- Response objects to API
- Data transfer objects between layers
- Configuration objects

**Benefits**:
- No boilerplate code
- Guaranteed immutability
- All fields final by default
- Clear intent (this is data, not behavior)

### Code Example - Record Usage

**Request DTO (Record)**:
```java
public record CreateOrderRequest(
    @NotEmpty(message = "Product IDs required")
    List<Long> productIds,

    @Min(value = 1)
    int quantity,

    @Nullable
    String deliveryNotes
) { }
```

**Response DTO (Record)**:
```java
public record OrderResponse(
    long orderId,
    long totalPrice,
    String status,
    LocalDateTime createdAt
) { }
```

**API Resource Record**:
```java
public record ProductResource(
    long id,
    @NotBlank
    String name,
    long price,
    int quantity
) { }
```

### Record Structure

**Simple Record**:
```
public record OrderRequest(String productId, int quantity) { }
```

**Record with Methods**:
- Can add custom methods
- Can add validation in compact constructor
- Cannot add mutable fields

**Record Usage**:
- Use in Presentation layer for request/response
- Use between layers for data transfer
- Prefer over mutable POJOs

## Immutable Data Structures

### Principles

**Final Fields**:
- All instance variables declared final
- Cannot be modified after construction
- Clear intent that object is immutable

**No Setters**:
- Immutable objects have no setter methods
- State set only in constructor
- Object state cannot change

**Defensive Copying** (when needed):
- If constructor takes mutable object
- Create copy instead of storing reference
- Prevents external mutation

### Benefits

1. **Thread Safety**: No synchronization needed
2. **Caching**: Can safely cache immutable objects
3. **Sharing**: Can safely share between threads
4. **Testing**: Simpler to test, no state changes
5. **Reasoning**: Easier to understand code behavior

### Common Immutable Types

- Strings: Immutable by design
- Records: Immutable by default
- Collections: Use Collections.unmodifiable*
- Primitive wrappers: Immutable
- LocalDate, LocalDateTime: Immutable

## Mutable Objects (When Needed)

### When Mutability is Acceptable

**Entities**:
- JPA entities may have setters for ORM
- But keep business logic in getter logic
- Minimize mutable state

**Builders**:
- Builder pattern creates immutable object
- Builder itself is mutable (intermediate)
- Final product is immutable

## Avoiding Mutable Shared State

### Pattern: Constructor Initialization

```
Instead of setters, initialize in constructor:
- All fields set once
- No post-construction state changes
- Clear dependencies visible in constructor
```

### Pattern: New Instance for Changes

```
Instead of modifying existing object:
- Create new instance with changed field
- Original remains unchanged
- Safer for multi-threaded code
```

### Pattern: Collections

- Use immutable collections when possible
- Use Collections.unmodifiable* wrappers
- Return new collection instead of modifying

## Best Practices

1. **Default to Immutable**: Start with immutable design
2. **Use Records for Data**: DTO objects use records
3. **Limit Mutability**: Only when necessary
4. **Document Intent**: Clearly state if object is mutable
5. **Avoid Shared Mutable State**: Between threads or layers

## Trade-offs

**Immutable Advantages**:
- Safer (no unexpected changes)
- Easier to reason about
- Better for concurrent code
- Can be cached/shared

**Immutable Disadvantages**:
- More memory (creating new instances)
- Slightly more verbose (must construct fully)
- Not suitable for all patterns (builder pattern exception)
