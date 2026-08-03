package com.shopflow.producer.controller;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.producer.dto.OrderRequestDto;
import com.shopflow.producer.dto.OrderResponseDto;
import com.shopflow.producer.dto.OrderSearchCriteria;
import com.shopflow.producer.entity.OrderStatus;
import com.shopflow.producer.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Create, retrieve, search and cancel orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an order",
               description = "Validates, enriches with catalogue data, persists and publishes")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "422", description = "Invalid shipping destination")
    @ApiResponse(responseCode = "503", description = "Catalogue unavailable")
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping("/{orderRef}")
    @Operation(summary = "Fetch one order", description = "Served from Redis when cached")
    @ApiResponse(responseCode = "200", description = "Order found")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable("orderRef") String orderRef) {
        return ResponseEntity.ok(orderService.findByRef(orderRef));
    }

    @GetMapping
    @Operation(summary = "Search orders", description = "Paginated, optionally filtered")
    @ApiResponse(responseCode = "200", description = "Page of orders")
    public ResponseEntity<Page<OrderResponseDto>> search(
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "shippingType", required = false) ShippingType shippingType,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(
                orderService.search(new OrderSearchCriteria(status, shippingType), pageable));
    }

    @PatchMapping("/{orderRef}/cancel")
    @Operation(summary = "Cancel an order", description = "Evicts the cached entry")
    @ApiResponse(responseCode = "200", description = "Order cancelled")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "409", description = "Illegal state transition")
    public ResponseEntity<OrderResponseDto> cancel(@PathVariable("orderRef") String orderRef) {
        return ResponseEntity.ok(orderService.cancel(orderRef));
    }
}