package com.letsbuy.catalog.service;

import com.letsbuy.catalog.domain.Category;
import com.letsbuy.catalog.domain.Product;
import com.letsbuy.catalog.domain.ProductStatus;
import com.letsbuy.catalog.persistence.CategoryRepository;
import com.letsbuy.catalog.persistence.ProductRepository;
import com.letsbuy.catalog.web.dto.CategoryResponse;
import com.letsbuy.catalog.web.dto.CreateCategoryRequest;
import com.letsbuy.catalog.web.dto.ProductResponse;
import com.letsbuy.catalog.web.dto.UpsertProductRequest;
import com.letsbuy.common.api.PageResponse;
import com.letsbuy.common.exception.ConflictException;
import com.letsbuy.common.exception.NotFoundException;
import com.letsbuy.common.money.Money;
import com.letsbuy.common.text.Slugs;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CatalogService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categories.findAll(Sort.by("name")).stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.name().trim());
        category.setDescription(blank(request.description()));
        category.setSlug(uniqueCategorySlug(Slugs.from(category.getName())));
        return CategoryResponse.from(categories.save(category));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String categorySlug, String query, int page, int size) {
        Page<Product> result = products.searchActive(
                orEmpty(categorySlug),
                orEmpty(query),
                PageRequest.of(page, size, Sort.by("name").ascending())
        );
        return map(result);
    }

    @Transactional(readOnly = true)
    public ProductResponse getActiveBySlug(String slug) {
        Product product = products.findBySlug(slug).orElseThrow(() -> new NotFoundException("Product not found."));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new NotFoundException("Product not found.");
        }
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> adminList(String status, int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> result = status == null || status.isBlank()
                ? products.findAll(request)
                : products.findByStatus(parseStatus(status), request);
        return map(result);
    }

    @Transactional
    public ProductResponse createProduct(UpsertProductRequest request) {
        if (products.existsBySku(request.sku().trim())) {
            throw new ConflictException("SKU already exists.");
        }
        Product product = new Product();
        apply(product, request);
        product.setSlug(uniqueProductSlug(Slugs.from(product.getName())));
        return ProductResponse.from(products.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(java.util.UUID id, UpsertProductRequest request) {
        Product product = products.findById(id).orElseThrow(() -> new NotFoundException("Product not found."));
        product.getCategory().getName();
        String sku = request.sku().trim();
        if (!sku.equals(product.getSku()) && products.existsBySku(sku)) {
            throw new ConflictException("SKU already exists.");
        }
        apply(product, request);
        return ProductResponse.from(product);
    }

    private void apply(Product product, UpsertProductRequest request) {
        Category category = categories.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found."));
        product.setCategory(category);
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setPriceMinor(Money.majorToMinor(request.price()));
        product.setCurrency("INR");
        product.setStockQuantity(request.stockQuantity());
        product.setStatus(parseStatus(request.status()));
        product.setImageUrl(blank(request.imageUrl()));
    }

    private String uniqueCategorySlug(String base) {
        return unique(base, categories::existsBySlug);
    }

    private String uniqueProductSlug(String base) {
        return unique(base, products::existsBySlug);
    }

    private String unique(String base, java.util.function.Predicate<String> exists) {
        String candidate = base;
        int suffix = 2;
        while (exists.test(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private static ProductStatus parseStatus(String status) {
        try {
            return ProductStatus.valueOf(status.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ConflictException("Status must be DRAFT, ACTIVE, or ARCHIVED.");
        }
    }

    private static PageResponse<ProductResponse> map(Page<Product> result) {
        return new PageResponse<>(
                result.getContent().stream().map(ProductResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.hasNext()
        );
    }

    private static String orEmpty(String value) {
        String trimmed = blank(value);
        return trimmed == null ? "" : trimmed;
    }

    private static String blank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
