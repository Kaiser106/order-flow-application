CREATE TABLE couriers (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          phone VARCHAR(20) NOT NULL,
                          active BOOLEAN DEFAULT FALSE,
                          current_location VARCHAR(255),
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP WITH TIME ZONE,
                          CONSTRAINT fk_courier_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);