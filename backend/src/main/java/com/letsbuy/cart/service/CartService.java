package com.letsbuy.cart.service;

import com.letsbuy.cart.api.CartLine;
import com.letsbuy.cart.api.CartPort;
import com.letsbuy.cart.domain.Cart;
import com.letsbuy.cart.domain.CartItem;
import com.letsbuy.cart.persistence.CartRepository;
import com.letsbuy.cart.web.dto.CartLineResponse;
import com.letsbuy.cart.web.dto.CartResponse;
import com.letsbuy.catalog.api.ProductLookup;
import com.letsbuy.catalog.api.ProductSnapshot;
import com.letsbuy.common.exception.ConflictException;
import com.letsbuy.common.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService implements CartPort {

    private final CartRepository carts;
    private final ProductLookup products;

    public CartService(CartRepository carts, ProductLookup products) {
        this.carts = carts;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public CartResponse view(UUID userId) {
        Cart cart = carts.findByUserId(userId).orElse(null);
        if (cart == null) {
            return new CartResponse(List.of(), 0, "INR");
        }
        List<CartLineResponse> lines = new ArrayList<>();
        long subtotal = 0;
        String currency = "INR";
        for (CartItem item : cart.getItems()) {
            ProductSnapshot product = products.find(item.getProductId()).orElse(null);
            if (product == null) {
                continue;
            }
            boolean available = product.purchasable();
            long line = available ? product.priceMinor() * item.getQuantity() : 0;
            if (available) {
                subtotal += line;
                currency = product.currency();
            }
            lines.add(new CartLineResponse(
                    item.getId(),
                    product.id(),
                    product.name(),
                    product.slug(),
                    product.imageUrl(),
                    product.priceMinor(),
                    product.currency(),
                    item.getQuantity(),
                    product.stockQuantity(),
                    available,
                    line
            ));
        }
        return new CartResponse(lines, subtotal, currency);
    }

    @Transactional
    public CartResponse add(UUID userId, UUID productId, int quantity) {
        ProductSnapshot product = products.requirePurchasable(productId);
        if (product.stockQuantity() < quantity) {
            throw new ConflictException("Not enough stock for " + product.name() + ".");
        }
        Cart cart = getOrCreate(userId);
        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
        int next = quantity;
        if (existing != null) {
            next = existing.getQuantity() + quantity;
        }
        if (next > 99 || next > product.stockQuantity()) {
            throw new ConflictException("You can add at most " + Math.min(99, product.stockQuantity()) + " of " + product.name() + ".");
        }
        if (existing == null) {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        } else {
            existing.setQuantity(next);
        }
        carts.save(cart);
        return view(userId);
    }

    @Transactional
    public CartResponse updateQuantity(UUID userId, UUID itemId, int quantity) {
        Cart cart = carts.findByUserId(userId).orElseThrow(() -> new NotFoundException("Cart item not found."));
        CartItem item = cart.getItems().stream()
                .filter(candidate -> candidate.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found."));
        ProductSnapshot product = products.requirePurchasable(item.getProductId());
        if (quantity > product.stockQuantity()) {
            throw new ConflictException("Not enough stock for " + product.name() + ".");
        }
        item.setQuantity(quantity);
        return view(userId);
    }

    @Transactional
    public CartResponse remove(UUID userId, UUID itemId) {
        Cart cart = carts.findByUserId(userId).orElseThrow(() -> new NotFoundException("Cart item not found."));
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new NotFoundException("Cart item not found.");
        }
        return view(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartLine> lines(UUID userId) {
        return carts.findByUserId(userId)
                .map(cart -> cart.getItems().stream().map(item -> new CartLine(item.getProductId(), item.getQuantity())).toList())
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void clear(UUID userId) {
        carts.findByUserId(userId).ifPresent(cart -> cart.getItems().clear());
    }

    private Cart getOrCreate(UUID userId) {
        return carts.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return carts.save(cart);
        });
    }
}
