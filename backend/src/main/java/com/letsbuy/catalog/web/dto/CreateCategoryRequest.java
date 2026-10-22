package com.letsbuy.catalog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 400) String description
) {
}
