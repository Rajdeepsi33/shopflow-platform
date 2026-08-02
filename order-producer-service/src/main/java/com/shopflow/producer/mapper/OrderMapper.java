package com.shopflow.producer.mapper;

import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.producer.dto.OrderItemResponseDto;
import com.shopflow.producer.dto.OrderResponseDto;
import com.shopflow.producer.dto.ProductDto;
import com.shopflow.producer.entity.Order;
import com.shopflow.producer.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Entity to DTO conversion, generated at compile time.
 *
 * unmappedTargetPolicy = ERROR means every target field must be
 * accounted for, so adding a field without mapping it breaks the build
 * rather than silently producing a null.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    OrderResponseDto toResponse(Order entity);

    OrderItemResponseDto toItemResponse(OrderItem entity);

    List<OrderItemResponseDto> toItemResponseList(List<OrderItem> entities);

    /**
     * Builds a line item from catalogue data plus the requested quantity.
     * The line total is computed here so the price always comes from the
     * catalogue, never from the client.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productTitle", source = "product.title")
    @Mapping(target = "unitPrice", source = "product.price")
    @Mapping(target = "quantity", source = "qty")
    @Mapping(target = "lineTotal",
             expression = "java(product.price().multiply(java.math.BigDecimal.valueOf(qty)))")
    OrderItem toItem(ProductDto product, Integer qty);

    /**
     * Entity to outbound event. Event metadata (id, type, timestamp,
     * schema version) is set by the publisher, not here.
     */
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "occurredAt", ignore = true)
    @Mapping(target = "schemaVersion", ignore = true)
    OrderCreatedEvent toEvent(Order entity);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productTitle", source = "productTitle")
    OrderCreatedEvent.Item toEventItem(OrderItem entity);
}