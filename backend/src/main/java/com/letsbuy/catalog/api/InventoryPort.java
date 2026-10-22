package com.letsbuy.catalog.api;

import java.util.UUID;

public interface InventoryPort {

    ProductSnapshot reserve(UUID productId, int quantity);

    void restock(UUID productId, int quantity);
}
