CREATE TABLE orders (
                        id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                        customer_id UUID NOT NULL,
                        restaurant_id UUID NOT NULL,
                        courier_id UUID,
                        status VARCHAR(50) NOT NULL,
                        total_price DECIMAL(10, 2) NOT NULL,
                        delivery_address TEXT NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP WITH TIME ZONE,
                        CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
                        CONSTRAINT fk_order_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
                        CONSTRAINT fk_order_courier FOREIGN KEY (courier_id) REFERENCES couriers (id)
);