package com.letsbuy.catalog.web;

import com.letsbuy.catalog.service.CatalogService;
import com.letsbuy.catalog.web.dto.CategoryResponse;
import com.letsbuy.catalog.web.dto.CreateCategoryRequest;
import com.letsbuy.catalog.web.dto.ProductResponse;
import com.letsbuy.catalog.web.dto.UpsertProductRequest;
import com.letsbuy.common.api.ApiResponse;
import com.letsbuy.common.api.PageRequests;
import com.letsbuy.common.api.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogController {

    private final CatalogService catalog;

    public AdminCatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.of(catalog.createCategory(request));
    }

    @GetMapping("/products")
    ApiResponse<PageResponse<ProductResponse>> products(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(catalog.adminList(status, PageRequests.clampPage(page), PageRequests.clampSize(size)));
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<ProductResponse> createProduct(@Valid @RequestBody UpsertProductRequest request) {
        return ApiResponse.of(catalog.createProduct(request));
    }

    @PutMapping("/products/{id}")
    ApiResponse<ProductResponse> updateProduct(@PathVariable UUID id, @Valid @RequestBody UpsertProductRequest request) {
        return ApiResponse.of(catalog.updateProduct(id, request));
    }
}
