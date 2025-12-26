# API Documentation Guide

SpringDoc OpenAPI library is used to automatically generate OpenAPI 3.0 specifications in Spring projects.

## Core Annotations

### @Operation

Documents individual endpoint behavior and purpose.

**Properties**:
- `summary`: Brief description (required)
- `description`: Detailed explanation (optional)
- `tags`: Endpoint classification (optional)

### @ApiResponse

Defines response contracts for different HTTP status codes.

**Properties**:
- `responseCode`: HTTP status code
- `description`: Response description
- `content`: Response schema definition

### @Schema

Documents data model fields and their purposes.

**Properties**:
- `description`: Field explanation
- `example`: Sample data
- `nullable`: Null handling
- `implementation`: Reference to another class

### @Parameter

Documents request parameters (path, query, header).

**Properties**:
- `name`: Parameter name
- `description`: Parameter explanation
- `example`: Sample value
- `required`: Requirement flag

## Code Example

```java
/**
 * 주문 관리 API Controller.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * 새로운 주문을 생성합니다.
     *
     * @param request 주문 생성 요청 정보
     * @return 생성된 주문 정보 (ID, 총액)
     */
    @Operation(summary = "주문 생성")
    @ApiResponse(responseCode = "201", description = "주문이 성공적으로 생성됨")
    @ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패")
    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return new OrderResponse(order.getId(), order.getTotalPrice());
    }

    /**
     * 주문 ID로 주문 정보를 조회합니다.
     *
     * @param orderId 조회할 주문의 ID
     * @return 주문 정보
     */
    @Operation(summary = "주문 조회")
    @ApiResponse(responseCode = "200", description = "주문 조회 성공")
    @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
        @Parameter(description = "조회할 주문의 고유 ID")
        @PathVariable long orderId
    ) {
        return orderService.getOrder(orderId);
    }
}
```

**Request DTO with Schema**:
```java
/**
 * 주문 생성 요청 DTO.
 * 프론트엔드에서 주문을 생성할 때 전달하는 데이터입니다.
 */
@Schema(description = "주문 생성 요청")
public record CreateOrderRequest(
    @Schema(description = "주문할 상품의 ID 목록", example = "[1, 2, 3]")
    @NotEmpty(message = "최소 1개 이상의 상품을 선택해야 합니다")
    List<Long> productIds,

    @Schema(description = "주문 수량", example = "5")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    int quantity,

    @Schema(description = "배송 요청사항 (선택사항)", example = "빠른 배송 부탁합니다")
    @Nullable
    String deliveryNotes
) { }
```

**Response DTO with Schema**:
```java
/**
 * 주문 응답 DTO.
 * API에서 주문 정보를 반환할 때 사용하는 데이터입니다.
 */
@Schema(description = "주문 정보 응답")
public record OrderResponse(
    @Schema(description = "생성된 주문의 고유 ID", example = "12345")
    long orderId,

    @Schema(description = "최종 결제 금액 (할인 적용됨)", example = "15000")
    long totalPrice,

    @Schema(description = "현재 주문 상태", example = "CONFIRMED")
    String status,

    @Schema(description = "주문 생성 시간", example = "2025-12-24T10:30:00")
    LocalDateTime createdAt
) { }
```

## Best Practices

1. **@Operation on every endpoint**: Document all public endpoints
2. **@Schema on DTO fields**: Describe data structure
3. **@ApiResponse for all outcomes**: Document success and error cases
4. **Clear descriptions**: Concise, meaningful explanations
5. **Realistic examples**: Use actual possible data values
