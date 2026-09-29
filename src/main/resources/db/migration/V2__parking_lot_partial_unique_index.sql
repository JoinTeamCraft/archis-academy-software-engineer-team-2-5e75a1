-- =============================================================================
-- V2 - Partial unique index on parking_lots (name, address)
--      for ACTIVE parking lots only.
--
-- A standard UNIQUE constraint cannot express a WHERE clause, so this rule
-- is implemented as a PostgreSQL partial unique index.
--
-- Effect:
--   * Two ACTIVE lots with the same name + address are rejected.
--   * INACTIVE lots are excluded from the index.
--   * Multiple INACTIVE lots with the same name + address are allowed.
--   * A new ACTIVE lot can reuse the name + address of an INACTIVE lot.
--
-- Referenced by:
--   * ParkingLot.ACTIVE_NAME_ADDRESS_UNIQUE_INDEX
--   * GlobalExceptionHandler#isParkingLotDuplicate
-- =============================================================================

CREATE UNIQUE INDEX uk_parking_lot_active_name_address
    ON parking_lots (name, address)
    WHERE status = 'ACTIVE';