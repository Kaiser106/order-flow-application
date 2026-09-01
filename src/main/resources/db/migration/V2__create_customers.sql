CREATE TABLE customers (
                           id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                           user_id UUID NOT NULL UNIQUE,
                           first_name VARCHAR(100) NOT NULL,
                           last_name VARCHAR(100) NOT NULL,
                           phone VARCHAR(20) NOT NULL,
                           address TEXT NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           deleted_at TIMESTAMP WITH TIME ZONE,
                           CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);