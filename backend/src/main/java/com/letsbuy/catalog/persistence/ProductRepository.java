package com.letsbuy.catalog.persistence;

import com.letsbuy.catalog.domain.Product;
import com.letsbuy.catalog.domain.ProductStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findBySlug(String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p join fetch p.category where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);

    @EntityGraph(attributePaths = "category")
    @Query(
            value = """
                    select p from Product p
                    where p.status = com.letsbuy.catalog.domain.ProductStatus.ACTIVE
                      and (:category = '' or p.category.slug = :category)
                      and (:term = '' or lower(p.name) like lower(concat('%', :term, '%'))
                           or lower(p.sku) like lower(concat('%', :term, '%')))
                    """,
            countQuery = """
                    select count(p) from Product p
                    where p.status = com.letsbuy.catalog.domain.ProductStatus.ACTIVE
                      and (:category = '' or p.category.slug = :category)
                      and (:term = '' or lower(p.name) like lower(concat('%', :term, '%'))
                           or lower(p.sku) like lower(concat('%', :term, '%')))
                    """
    )
    Page<Product> searchActive(@Param("category") String category, @Param("term") String term, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);
}
