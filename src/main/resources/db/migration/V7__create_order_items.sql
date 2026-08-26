CREATE TABLE order_items (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             unit_price DECIMAL(10, 2) NOT NULL,
                             total_price DECIMAL(10, 2) NOT NULL,
                             CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products (id)
);