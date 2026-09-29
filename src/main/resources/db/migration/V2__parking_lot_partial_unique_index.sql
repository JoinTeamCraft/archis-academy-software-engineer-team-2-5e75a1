-- =============================================================================
-- V2 - Partial unique index on parking_lots (name, address) WHERE status = 'ACTIVE'
--
-- A standard UNIQUE constraint cannot express a WHERE clause, so this partial
-- unique index is created here rather than via @UniqueConstraint in JPA.
--
-- Effect:
--   * Two ACTIVE lots with the same name + address are rejected.
--   * INACTIVE lots are excluded from the index.
--   * Multiple INACTIVE lots with the same name + address are therefore allowed.
--   * A new ACTIVE lot can reuse the name + address of an INACTIVE lot.
--
-- Referenced by:
--   * ParkingLot.ACTIVE_NAME_ADDRESS_UNIQUE_INDEX
--   * GlobalExceptionHandler#isParkingLotDuplicate
--     (maps this database violation to HTTP 409)
-- =============================================================================

CREATE UNIQUE INDEX uk_parking_lot_active_name_address
    ON parking_lots (name, address)
    WHERE status = 'ACTIVE';