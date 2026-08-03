package com.shopflow.producer.service;

import com.shopflow.producer.dto.OrderRequestDto;
import com.shopflow.producer.dto.OrderResponseDto;
import com.shopflow.producer.dto.OrderSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto request);

    OrderResponseDto findByRef(String orderRef);

    Page<OrderResponseDto> search(OrderSearchCriteria criteria, Pageable pageable);

    OrderResponseDto cancel(String orderRef);

    void markPublished(String orderRef);

    void markPublishFailed(String orderRef);
}