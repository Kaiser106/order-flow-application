CREATE TABLE order_items (
                             id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                             order_id UUID NOT NULL,
                             product_id UUID NOT NULL,
                             quantity INT NOT NULL,
                             unit_price DECIMAL(10, 2) NOT NULL,
                             total_price DECIMAL(10, 2) NOT NULL,
                             CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products (id)
);