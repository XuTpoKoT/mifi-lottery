-- Создание таблиц
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS draws (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    winning_combination VARCHAR(50),
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    draw_id BIGINT NOT NULL REFERENCES draws(id),
    user_id BIGINT REFERENCES users(id),
    ticket_number VARCHAR(20) NOT NULL,
    combination VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(draw_id, ticket_number)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- Индексы
CREATE INDEX idx_draws_status_start ON draws(status, start_time);
CREATE INDEX idx_payments_ticket ON payments(ticket_id);

-- Добавим тестового администратора (пароль: admin123, захеширован BCrypt)
INSERT INTO users (username, password_hash, role) VALUES
('admin', '$2a$10$FhVHzVyHRZzd6lrT4KIYG.CinbjMRI0ma/F5BFFjnhOL99HBjs/hK', 'ADMIN');