CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(32)     NOT NULL UNIQUE,
    name            VARCHAR(200)    NOT NULL,
    description     VARCHAR(1000),
    category        VARCHAR(64)     NOT NULL,
    price           NUMERIC(10, 2)  NOT NULL,
    stock_quantity  INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_name ON products (name);

CREATE TABLE customers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    email           VARCHAR(200)    NOT NULL UNIQUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT          NOT NULL REFERENCES customers (id),
    status          VARCHAR(32)     NOT NULL,
    total_amount    NUMERIC(10, 2)  NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES orders (id),
    product_id      BIGINT          NOT NULL REFERENCES products (id),
    product_name    VARCHAR(200)    NOT NULL,
    unit_price      NUMERIC(10, 2)  NOT NULL,
    quantity        INTEGER         NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
