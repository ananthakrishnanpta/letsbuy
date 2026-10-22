package com.letsbuy.ordering.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

    @Column(name = "unit_price_minor", nullable = false)
    private long unitPriceMinor;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_total_minor", nullable = false)
    private long lineTotalMinor;

    public UUID getId() {
        return id;
    }

    public void setOrder(CustomerOrder order) {
        this.order = order;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getUnitPriceMinor() {
        return unitPriceMinor;
    }

    public void setUnitPriceMinor(long unitPriceMinor) {
        this.unitPriceMinor = unitPriceMinor;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getLineTotalMinor() {
        return lineTotalMinor;
    }

    public void setLineTotalMinor(long lineTotalMinor) {
        this.lineTotalMinor = lineTotalMinor;
    }
}
