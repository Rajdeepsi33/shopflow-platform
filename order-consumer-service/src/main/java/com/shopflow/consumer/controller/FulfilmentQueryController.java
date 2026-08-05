package com.shopflow.consumer.controller;

import com.shopflow.consumer.dto.FulfilmentResponseDto;
import com.shopflow.consumer.exception.FulfilmentNotFoundException;
import com.shopflow.consumer.mapper.FulfilmentMapper;
import com.shopflow.consumer.repository.FulfilmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fulfilments")
@RequiredArgsConstructor
@Tag(name = "Fulfilments", description = "Query processed fulfilments")
public class FulfilmentQueryController {

    private final FulfilmentRepository fulfilmentRepository;
    private final FulfilmentMapper fulfilmentMapper;

    @GetMapping("/{orderRef}")
    @Transactional(readOnly = true)
    @Operation(summary = "Fetch the fulfilment for an order")
    @ApiResponse(responseCode = "200", description = "Fulfilment found")
    @ApiResponse(responseCode = "404", description = "No fulfilment for that order")
    public ResponseEntity<FulfilmentResponseDto> getByOrderRef(
            @PathVariable("orderRef") String orderRef) {

        return fulfilmentRepository.findByOrderRef(orderRef)
                .map(fulfilmentMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new FulfilmentNotFoundException(orderRef));
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "List fulfilments by warehouse")
    @ApiResponse(responseCode = "200", description = "Matching fulfilments")
    public ResponseEntity<List<FulfilmentResponseDto>> getByWarehouse(
            @RequestParam(name = "warehouseCode") String warehouseCode) {

        return ResponseEntity.ok(fulfilmentMapper.toResponseList(
                fulfilmentRepository.findByWarehouseCode(warehouseCode)));
    }
}