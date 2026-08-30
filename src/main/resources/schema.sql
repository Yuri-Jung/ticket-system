CREATE TABLE IF NOT EXISTS USERS (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS CONCERTS (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_concerts_artist (artist)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS CONCERT_SCHEDULES (
    id BIGINT NOT NULL AUTO_INCREMENT,
    concert_id BIGINT NOT NULL,
    concert_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_concert_schedules_concert_id (concert_id),
    KEY idx_concert_schedules_concert_at (concert_at),
    CONSTRAINT fk_concert_schedules_concert_id
        FOREIGN KEY (concert_id) REFERENCES CONCERTS (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS SEATS (
    id BIGINT NOT NULL AUTO_INCREMENT,
    concert_schedule_id BIGINT NOT NULL,
    seat_number VARCHAR(50) NOT NULL,
    grade VARCHAR(50) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    held_by_user_id BIGINT NULL,
    held_until DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_seats_schedule_seat_number (concert_schedule_id, seat_number),
    KEY idx_seats_schedule_id (concert_schedule_id),
    KEY idx_seats_status (status),
    KEY idx_seats_held_until (held_until),
    KEY idx_seats_held_by_user_id (held_by_user_id),
    CONSTRAINT fk_seats_concert_schedule_id
        FOREIGN KEY (concert_schedule_id) REFERENCES CONCERT_SCHEDULES (id),
    CONSTRAINT fk_seats_held_by_user_id
        FOREIGN KEY (held_by_user_id) REFERENCES USERS (id),
    CONSTRAINT chk_seats_status
        CHECK (status IN ('AVAILABLE', 'HELD', 'RESERVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS SEAT_HOLDS (
    id BIGINT NOT NULL AUTO_INCREMENT,
    seat_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    held_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_seat_holds_seat_id (seat_id),
    KEY idx_seat_holds_user_id (user_id),
    KEY idx_seat_holds_expires_at (expires_at),
    KEY idx_seat_holds_status (status),
    CONSTRAINT fk_seat_holds_seat_id
        FOREIGN KEY (seat_id) REFERENCES SEATS (id),
    CONSTRAINT fk_seat_holds_user_id
        FOREIGN KEY (user_id) REFERENCES USERS (id),
    CONSTRAINT chk_seat_holds_status
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'RELEASED', 'ORDERED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ORDERS (
    id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_orders_user_id (user_id),
    KEY idx_orders_seat_id (seat_id),
    KEY idx_orders_status (status),
    KEY idx_orders_created_at (created_at),
    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (user_id) REFERENCES USERS (id),
    CONSTRAINT fk_orders_seat_id
        FOREIGN KEY (seat_id) REFERENCES SEATS (id),
    CONSTRAINT chk_orders_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS PAYMENTS (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    pg_transaction_id VARCHAR(128) NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failed_reason VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_idempotency_key (idempotency_key),
    KEY idx_payments_order_id (order_id),
    KEY idx_payments_pg_transaction_id (pg_transaction_id),
    CONSTRAINT fk_payments_order_id
        FOREIGN KEY (order_id) REFERENCES ORDERS (id),
    CONSTRAINT chk_payments_status
        CHECK (status IN ('READY', 'SUCCESS', 'FAILED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS OUTBOX_EVENTS (
    event_id VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (event_id),
    KEY idx_outbox_events_status (status),
    KEY idx_outbox_events_created_at (created_at),
    KEY idx_outbox_events_aggregate (aggregate_type, aggregate_id),
    CONSTRAINT chk_outbox_events_status
        CHECK (status IN ('INIT', 'PUBLISHED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS PROCESSED_EVENTS (
    event_id VARCHAR(64) NOT NULL,
    handler_name VARCHAR(100) NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (event_id),
    KEY idx_processed_events_handler_name (handler_name),
    KEY idx_processed_events_processed_at (processed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS SAGA_INSTANCES (
    saga_id VARCHAR(64) NOT NULL,
    current_step VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload JSON NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (saga_id),
    KEY idx_saga_instances_status (status),
    KEY idx_saga_instances_updated_at (updated_at),
    CONSTRAINT chk_saga_instances_status
        CHECK (status IN ('STARTED', 'COMPENSATING', 'COMPLETED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
