package com.letsbuy.cart.api;

import java.util.List;
import java.util.UUID;

public interface CartPort {

    List<CartLine> lines(UUID userId);

    void clear(UUID userId);
}
