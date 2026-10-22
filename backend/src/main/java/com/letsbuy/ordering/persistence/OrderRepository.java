package com.letsbuy.ordering.persistence;

import com.letsbuy.ordering.domain.CustomerOrder;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

    Page<CustomerOrder> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findWithItemsByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findWithItemsById(UUID id);

    boolean existsByOrderNumber(String orderNumber);
}
