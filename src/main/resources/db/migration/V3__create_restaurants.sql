CREATE TABLE restaurants (
                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             user_id BIGINT NOT NULL UNIQUE,
                             name VARCHAR(255) NOT NULL,
                             description TEXT,
                             phone VARCHAR(20) NOT NULL,
                             email VARCHAR(255) NOT NULL,
                             address JSONB NOT NULL,
                             working_hours JSONB NOT NULL,
                             active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             deleted_at TIMESTAMP WITH TIME ZONE,
                             CONSTRAINT fk_restaurant_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);