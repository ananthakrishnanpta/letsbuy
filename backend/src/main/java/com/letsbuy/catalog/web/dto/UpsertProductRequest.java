package com.letsbuy.catalog.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record UpsertProductRequest(
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 4000) String description,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @Min(0) @Max(1_000_000) Integer stockQuantity,
        @NotBlank @Size(max = 32) String status,
        @Size(max = 500) String imageUrl
) {
}
