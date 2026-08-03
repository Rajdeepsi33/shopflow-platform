package com.shopflow.producer.controller;

import com.shopflow.producer.dto.ProductDto;
import com.shopflow.producer.service.ProductCatalogClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Product catalogue proxy with cache and circuit breaker")
public class ProductController {

    private final ProductCatalogClient productCatalogClient;

    @GetMapping("/{productId}")
    @Operation(summary = "Fetch a product",
               description = "Read-through cache; serves stale data when the breaker is open")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "503", description = "Catalogue unavailable and nothing cached")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(productCatalogClient.fetchProduct(productId));
    }
}