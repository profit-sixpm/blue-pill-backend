# JPA Guidelines

## Avoid JPA Relationship Annotations

### Problem with Relationships

**@OneToMany, @ManyToOne, @ManyToMany annotations cause**:
- Lazy loading exceptions
- N+1 query problems
- Circular reference issues
- Tight coupling between entities
- Unexpected eager loading

### Recommended Approach

**Store Foreign Key IDs Instead**:
- Keep only the ID reference as a primitive or wrapper type
- Fetch related entities explicitly when needed
- Full control over query strategy

### Code Example - Entity Design

**Base Entity Pattern**:
```java
/**
 * 모든 엔티티의 기본 추상 클래스.
 * 공통 필드인 id, createdAt, updatedAt을 포함합니다.
 */
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA에서 엔티티 업데이트 시 자동으로 호출되어 updatedAt을 갱신합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
```

**Order Entity with Domain Logic**:
```java
/**
 * 주문(Order) 엔티티.
 * 사용자가 상품을 구매한 주문 정보를 나타냅니다.
 */
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    private long userId;              // Store user ID, not relationship
    private long totalPrice;          // Total product price
    private String status;            // Order status (PENDING, CONFIRMED, SHIPPED, DELIVERED)

    @Column(name = "discount_amount")
    private long discountAmount;      // Discount amount applied

    @Column(name = "final_price")
    private long finalPrice;          // Final payment price after discount

    protected Order() {
    }

    /**
     * 주문을 생성합니다.
     *
     * @param userId 주문자 ID
     * @param totalPrice 상품의 총 금액
     */
    public Order(long userId, long totalPrice) {
        super();
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING.getValue();
        this.discountAmount = 0;
        this.finalPrice = totalPrice;
    }

    /**
     * 주문의 최종 금액을 계산합니다.
     * 할인액이 적용된 최종 결제금액을 반환합니다.
     *
     * @return 할인이 적용된 최종 결제 금액
     */
    public long calculateTotal() {
        return totalPrice - discountAmount;
    }

    /**
     * 주문에 할인을 적용합니다.
     *
     * @param discountAmount 할인할 금액
     * @throws IllegalArgumentException when discount exceeds total price
     */
    public void applyDiscount(long discountAmount) {
        if (discountAmount > totalPrice) {
            throw new IllegalArgumentException("Discount amount cannot exceed total price");
        }
        this.discountAmount = discountAmount;
        this.finalPrice = calculateTotal();
    }

    /**
     * 주문 상태를 '확정'으로 변경합니다.
     * 대기 중인 주문만 확정할 수 있습니다.
     *
     * @throws IllegalStateException when order is not in PENDING status
     */
    public void confirm() {
        if (!OrderStatus.PENDING.getValue().equals(this.status)) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED.getValue();
    }

    /**
     * 주문 상태를 '배송 중'으로 변경합니다.
     * 확정된 주문만 배송할 수 있습니다.
     *
     * @throws IllegalStateException when order is not in CONFIRMED status
     */
    public void ship() {
        if (!OrderStatus.CONFIRMED.getValue().equals(this.status)) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }
        this.status = OrderStatus.SHIPPED.getValue();
    }

    /**
     * 주문 상태를 '배송 완료'로 변경합니다.
     * 배송 중인 주문만 배송 완료 처리할 수 있습니다.
     *
     * @throws IllegalStateException when order is not in SHIPPED status
     */
    public void deliver() {
        if (!OrderStatus.SHIPPED.getValue().equals(this.status)) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        this.status = OrderStatus.DELIVERED.getValue();
    }

    /**
     * 주문이 특정 상태인지 확인합니다.
     *
     * @param status 확인할 상태 값
     * @return 주문이 해당 상태이면 true, 아니면 false
     */
    public boolean isStatus(String status) {
        return this.status.equals(status);
    }

    public long getUserId() {
        return userId;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public long getFinalPrice() {
        return finalPrice;
    }
}
```

**OrderStatus Enum**:
```java
/**
 * 주문의 상태를 나타내는 열거형입니다.
 */
public enum OrderStatus {
    PENDING("PENDING", "대기중"),
    CONFIRMED("CONFIRMED", "확정됨"),
    SHIPPED("SHIPPED", "배송중"),
    DELIVERED("DELIVERED", "배송완료");

    private final String value;
    private final String description;

    OrderStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
```

**DO NOT: Use Relationship Annotations**:
```java
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.LAZY)  // Avoid! Can cause lazy loading exceptions
    private User user;

    @OneToMany(mappedBy = "order")      // Avoid! Can cause N+1 query problems
    private List<OrderItem> items;
}
```

**Repository - Explicit Fetching**:
```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.userId = :userId")
    List<Order> findByUserId(long userId);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findOrderWithDetails(long orderId);
}
```

**Service - Explicit Data Loading**:
```java
/**
 * 주문 비즈니스 로직을 처리하는 서비스.
 * 저장소를 통해 엔티티를 조회하고 도메인 로직을 조합합니다.
 */
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * 주문 ID로 주문과 사용자 정보를 함께 조회합니다.
     * 두 개의 독립적인 쿼리를 실행하여 N+1 문제를 피합니다.
     *
     * @param orderId 조회할 주문 ID
     * @return 주문과 사용자 정보를 포함한 DTO
     * @throws OrderNotFoundException when order does not exist
     * @throws UserNotFoundException when user does not exist
     */
    public OrderWithUserDto getOrderWithUser(long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException());

        User user = userRepository.findById(order.getUserId())
            .orElseThrow(() -> new UserNotFoundException());

        return new OrderWithUserDto(order.getId(), user.getName(), order.getTotalPrice());
    }

    /**
     * 여러 주문을 조회할 때 배치 처리로 관련 사용자 정보를 함께 조회합니다.
     * 이를 통해 N+1 쿼리 문제를 효율적으로 해결합니다.
     *
     * @param orderIds 조회할 주문 ID 목록
     * @return 주문과 사용자 정보를 포함한 DTO 목록
     */
    public List<OrderWithUserDto> getOrdersWithUsers(List<Long> orderIds) {
        // 1. Fetch all orders with single query
        List<Order> orders = orderRepository.findAllById(orderIds);

        // 2. Extract unique user IDs
        List<Long> userIds = orders.stream()
            .map(Order::getUserId)
            .distinct()
            .toList();

        // 3. Fetch all users in single query and create map
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        // 4. Combine orders and users in memory
        return orders.stream()
            .map(order -> new OrderWithUserDto(
                order.getId(),
                users.get(order.getUserId()).getName(),
                order.getTotalPrice()
            ))
            .toList();
    }

    /**
     * 주문에 할인을 적용합니다.
     * Order 엔티티의 도메인 로직을 호출하여 할인을 처리합니다.
     *
     * @param orderId 할인을 적용할 주문 ID
     * @param discountAmount 할인 금액
     */
    @Transactional
    public void applyDiscount(long orderId, long discountAmount) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException());

        order.applyDiscount(discountAmount);
        // No explicit save needed - JPA detects changes
    }
}
```

## Explicit Entity Fetching

### Fetch Strategy

**When Needed**:
- Service layer explicitly fetches related entities
- Repository methods load related data
- Complete control over join strategies

**Benefits**:
- Prevents N+1 queries
- Clear what data is loaded
- Optimized for specific use cases
- No lazy loading exceptions

### Repository Methods

**Explicit Fetching Approach**:
- Method names indicate what they return
- Use JOIN FETCH or @Query annotations
- Load exactly what's needed

## Entity Design

### Field Handling

**Foreign Key Storage**:
- Store as primitive long or Long (wrapper)
- No @ManyToOne or @OneToMany annotations
- Clear that it's just an ID reference

**Entity Collections** (if needed):
- Use primitive ID collections
- Map via separate repository queries
- Don't use @OneToMany mappings

### Entity Responsibilities

**What Entities Contain**:
- Data fields (including ID references)
- Domain logic (calculations, validations)
- Computed properties (getters with logic)

**What Entities Don't Do**:
- Lazy load related entities
- Navigate relationships
- Make database calls

## Query Optimization

### N+1 Query Problem

**Anti-Pattern**:
- Loop through orders
- For each order, fetch user (lazy loading)
- Results in N+1 queries (1 parent + N children)

**Solution**:
- Fetch user IDs with orders
- Use separate query to fetch users
- Combine in service layer or use JOIN FETCH

### Fetch Join

**JOIN FETCH Strategy**:
- Load order with user in single query
- Prevents N+1 when relationship needed
- Only for specific queries needing both

### Separate Queries

**Multi-Query Strategy**:
- Fetch orders in one query
- Fetch users in another query
- Combine data in memory or service layer

## Repository Pattern

### Repository Interface

**Responsibilities**:
- Query entity by various criteria
- Persist/update entities
- Delete entities
- No business logic

**Naming Convention**:
- Find methods: findById, findByStatus, etc.
- Save/update: save, update
- Delete: deleteById, delete

### Custom Query Methods

**JPQL Query**:
- Use @Query for complex queries
- Specify exactly what to fetch
- Control JOIN FETCH behavior

**Native SQL** (when needed):
- For complex queries JPQL can't express
- Use sparingly
- Document why native SQL needed

## Common Patterns

### Find Single Entity

- By ID: findById
- By unique field: findByEmail
- Returns Optional<T>

### Find Multiple Entities

- All: findAll
- By criteria: findByStatus, findByDate
- Returns List<T>

### Count Operations

- Count all: count
- Count by criteria: countByStatus
- Returns long

## Testing Entities

**Entity Tests**:
- No database required
- Test business logic methods
- Test field initialization

**Repository Tests**:
- Use test database
- Test query correctness
- Test data persistence

**Service Tests** (with mocked repositories):
- Test orchestration logic
- Test calling correct repository methods
- Mock repository for unit tests

## Migration Considerations

### Schema Changes

**Adding Fields**:
- Use Flyway migration
- Existing data handling
- Set defaults for existing rows

**Removing Fields**:
- No longer referenced in code
- Remove from entity
- Migration to drop column

**Changing Types**:
- Must be compatible
- Migration to alter column
- Careful with data conversion
