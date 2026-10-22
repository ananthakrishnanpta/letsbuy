package com.letsbuy.catalog.api;

import java.util.Optional;
import java.util.UUID;

public interface ProductLookup {

    Optional<ProductSnapshot> find(UUID productId);

    ProductSnapshot requirePurchasable(UUID productId);
}
