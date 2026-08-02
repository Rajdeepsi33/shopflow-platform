package com.shopflow.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Product data from the catalogue")
public record ProductDto(
        Long id,
        String title,
        BigDecimal price,
        String category,
        String image,

        @Schema(description = "Where this data came from", example = "CATALOG")
        DataSource dataSource
) {

    public enum DataSource {
        CATALOG,
        CACHE,
        CACHE_FALLBACK
    }

    public ProductDto withDataSource(DataSource source) {
        return new ProductDto(id, title, price, category, image, source);
    }
}