-- =============================================================================
-- V2 - Partial unique index on parking_lots (name, address) WHERE status = 'ACTIVE'
--
-- A standard UNIQUE constraint cannot express a WHERE clause, so this index
-- is created here (in the migration) rather than via @UniqueConstraint in JPA.
--
-- Effect:
--   * Two lots with the SAME name+address are rejected only when BOTH are ACTIVE.
--   * Deactivated (INACTIVE) lots are excluded from the index, so the same
--     name+address combination can be reused if the previous lot is soft-deleted.
--
-- Referenced by:
--   * ParkingLot.UNIQUE_NAME_ADDRESS_CONSTRAINT  ("uk_parking_lot_name_address")
--   * GlobalExceptionHandler#isParkingLotDuplicate  (maps violation -> HTTP 409)
-- =============================================================================

CREATE UNIQUE INDEX uk_parking_lot_name_address
    ON parking_lots (name, address)
    WHERE status = 'ACTIVE';
