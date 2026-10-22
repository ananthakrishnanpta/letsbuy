package com.letsbuy.cart.persistence;

import com.letsbuy.cart.domain.Cart;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByUserId(UUID userId);
}
