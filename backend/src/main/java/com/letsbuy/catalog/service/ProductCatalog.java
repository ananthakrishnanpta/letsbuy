package com.letsbuy.catalog.service;

import com.letsbuy.catalog.api.InventoryPort;
import com.letsbuy.catalog.api.ProductLookup;
import com.letsbuy.catalog.api.ProductSnapshot;
import com.letsbuy.catalog.domain.Product;
import com.letsbuy.catalog.domain.ProductStatus;
import com.letsbuy.catalog.persistence.ProductRepository;
import com.letsbuy.common.exception.ConflictException;
import com.letsbuy.common.exception.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCatalog implements ProductLookup, InventoryPort {

    private final ProductRepository products;

    public ProductCatalog(ProductRepository products) {
        this.products = products;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductSnapshot> find(UUID productId) {
        return products.findById(productId).map(product -> {
            product.getCategory().getName();
            return CatalogMapper.snapshot(product);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSnapshot requirePurchasable(UUID productId) {
        Product product = products.findById(productId).orElseThrow(() -> new NotFoundException("Product not found."));
        product.getCategory().getName();
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ConflictException("This product is not available.");
        }
        return CatalogMapper.snapshot(product);
    }

    @Override
    @Transactional
    public ProductSnapshot reserve(UUID productId, int quantity) {
        Product product = products.findByIdForUpdate(productId).orElseThrow(() -> new NotFoundException("Product not found."));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ConflictException(product.getName() + " is not available.");
        }
        if (quantity < 1 || product.getStockQuantity() < quantity) {
            throw new ConflictException("Not enough stock for " + product.getName() + ".");
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return CatalogMapper.snapshot(product);
    }

    @Override
    @Transactional
    public void restock(UUID productId, int quantity) {
        Product product = products.findByIdForUpdate(productId).orElseThrow(() -> new NotFoundException("Product not found."));
        product.setStockQuantity(product.getStockQuantity() + quantity);
    }
}
