package com.letsbuy.catalog.web;

import com.letsbuy.catalog.service.CatalogService;
import com.letsbuy.catalog.web.dto.CategoryResponse;
import com.letsbuy.catalog.web.dto.ProductResponse;
import com.letsbuy.common.api.ApiResponse;
import com.letsbuy.common.api.PageRequests;
import com.letsbuy.common.api.PageResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.of(catalog.listCategories());
    }

    @GetMapping("/products")
    ApiResponse<PageResponse<ProductResponse>> products(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ApiResponse.of(catalog.search(category, q, PageRequests.clampPage(page), PageRequests.clampSize(size)));
    }

    @GetMapping("/products/{slug}")
    ApiResponse<ProductResponse> product(@PathVariable String slug) {
        return ApiResponse.of(catalog.getActiveBySlug(slug));
    }
}
