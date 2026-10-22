CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    full_name VARCHAR(120) NOT NULL,
    line1 VARCHAR(160) NOT NULL,
    line2 VARCHAR(160),
    city VARCHAR(80) NOT NULL,
    region VARCHAR(80) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX addresses_user_idx ON addresses (user_id);

CREATE TABLE carts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products (id),
    quantity INTEGER NOT NULL CHECK (quantity > 0 AND quantity <= 99),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT cart_items_product_unique UNIQUE (cart_id, product_id)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    order_number VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    subtotal_minor BIGINT NOT NULL,
    shipping_minor BIGINT NOT NULL,
    total_minor BIGINT NOT NULL,
    shipping_full_name VARCHAR(120) NOT NULL,
    shipping_line1 VARCHAR(160) NOT NULL,
    shipping_line2 VARCHAR(160),
    shipping_city VARCHAR(80) NOT NULL,
    shipping_region VARCHAR(80) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country_code VARCHAR(2) NOT NULL,
    shipping_phone VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT orders_status_check CHECK (status IN ('PLACED', 'FULFILLED', 'CANCELLED'))
);

CREATE INDEX orders_user_created_idx ON orders (user_id, created_at DESC);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products (id),
    sku VARCHAR(64) NOT NULL,
    product_name VARCHAR(160) NOT NULL,
    unit_price_minor BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    line_total_minor BIGINT NOT NULL
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE REFERENCES orders (id) ON DELETE CASCADE,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    method VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    provider_reference VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT payments_method_check CHECK (method IN ('COD')),
    CONSTRAINT payments_status_check CHECK (status IN ('PENDING', 'CAPTURED', 'VOIDED', 'FAILED'))
);
