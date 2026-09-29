-- V1 - Initial schema
-- Derived from JPA entity mappings. Tables are created in FK dependency order.

-- -------------------------------------------------------------------------
-- users
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL       PRIMARY KEY,
    name          VARCHAR(255)    NOT NULL,
    email         VARCHAR(255)    NOT NULL,
    password_hash VARCHAR(255)    NOT NULL,
    role          VARCHAR(50)     NOT NULL,
    created_at    TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'OPERATOR', 'CUSTOMER'))
);

-- -------------------------------------------------------------------------
-- vehicles
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    id            BIGSERIAL       PRIMARY KEY,
    user_id       BIGINT          NOT NULL,
    license_plate VARCHAR(255)    NOT NULL,
    type          VARCHAR(50)     NOT NULL,
    color         VARCHAR(255),
    created_at    TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_vehicles_license_plate UNIQUE (license_plate),
    CONSTRAINT fk_vehicles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_vehicles_type CHECK (type IN ('CAR', 'MOTORBIKE', 'TRUCK', 'EV'))
);

-- -------------------------------------------------------------------------
-- parking_lots
-- NOTE: the unique constraint on (name, address) WHERE status = 'ACTIVE'
--       is a *partial* index and therefore lives in V2, not here.
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parking_lots (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    capacity    INTEGER      NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    operator_id BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_parking_lots_operator FOREIGN KEY (operator_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_parking_lots_status  CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- -------------------------------------------------------------------------
-- parking_spots
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parking_spots (
    id          BIGSERIAL    PRIMARY KEY,
    lot_id      BIGINT       NOT NULL,
    spot_number VARCHAR(255) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'AVAILABLE',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_parking_spots_lot_spot  UNIQUE (lot_id, spot_number),
    CONSTRAINT fk_parking_spots_lot       FOREIGN KEY (lot_id)  REFERENCES parking_lots (id) ON DELETE RESTRICT,
    CONSTRAINT chk_parking_spots_type     CHECK (type   IN ('CAR', 'MOTORBIKE', 'TRUCK', 'EV')),
    CONSTRAINT chk_parking_spots_status   CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE'))
);

-- -------------------------------------------------------------------------
-- reservations
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    id          BIGSERIAL    PRIMARY KEY,
    vehicle_id  BIGINT       NOT NULL,
    spot_id     BIGINT       NOT NULL,
    customer_id BIGINT       NOT NULL,
    version     BIGINT,
    start_time  TIMESTAMP    NOT NULL,
    end_time    TIMESTAMP    NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reservations_vehicle  FOREIGN KEY (vehicle_id)  REFERENCES vehicles     (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reservations_spot     FOREIGN KEY (spot_id)     REFERENCES parking_spots (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id) REFERENCES users         (id) ON DELETE RESTRICT,
    CONSTRAINT chk_reservations_status  CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'COMPLETED')),
    CONSTRAINT chk_reservations_times   CHECK (end_time > start_time)
);

-- -------------------------------------------------------------------------
-- payments
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    id             BIGSERIAL       PRIMARY KEY,
    reservation_id BIGINT          NOT NULL,
    amount         NUMERIC(10, 2)  NOT NULL,
    currency       VARCHAR(10)     NOT NULL,
    method         VARCHAR(50)     NOT NULL,
    status         VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_payments_reservation UNIQUE (reservation_id),
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT,
    CONSTRAINT chk_payments_amount     CHECK (amount > 0),
    CONSTRAINT chk_payments_currency   CHECK (currency IN ('INR', 'USD')),
    CONSTRAINT chk_payments_method     CHECK (method   IN ('CASH', 'CARD', 'UPI', 'ONLINE')),
    CONSTRAINT chk_payments_status     CHECK (status   IN ('PENDING', 'PAID', 'REFUNDED', 'FAILED'))
);
