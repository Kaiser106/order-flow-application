CREATE TABLE products (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          restaurant_id BIGINT NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          price DECIMAL(10, 2) NOT NULL,
                          category VARCHAR(100) NOT NULL,
                          available BOOLEAN DEFAULT TRUE,
                          image_url TEXT,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP WITH TIME ZONE,
                          CONSTRAINT fk_product_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
);