package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParkingLotEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        User operator = new User();
        ParkingLot lot = new ParkingLot();
        lot.setId(1L);
        lot.setName("Downtown Lot");
        lot.setLocation("123 Main St");
        lot.setCapacity(500);
        lot.setStatus(ParkingLot.Status.ACTIVE);
        lot.setOperator(operator);

        assertEquals(1L, lot.getId());
        assertEquals("Downtown Lot", lot.getName());
        assertEquals("123 Main St", lot.getLocation());
        assertEquals(500, lot.getCapacity());
        assertEquals(ParkingLot.Status.ACTIVE, lot.getStatus());
        assertSame(operator, lot.getOperator());
    }

    // -------------------------------------------------------------------------
    // Operator-is-mandatory invariant
    // -------------------------------------------------------------------------

    @Test
    void setOperatorRejectsNull() {
        ParkingLot lot = new ParkingLot();
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> lot.setOperator(null)
        );
        assertEquals("operator must not be null", ex.getMessage());
    }

    @Test
    void setOperatorStoresNonNullUser() {
        User operator = new User();
        ParkingLot lot = new ParkingLot();
        lot.setOperator(operator);
        assertSame(operator, lot.getOperator());
    }

    @Test
    void joinColumnDeclaresNullableFalse() throws NoSuchFieldException {
        // Guards against accidentally changing nullable = false → true on the @JoinColumn,
        // which would silently weaken the DB-level NOT NULL constraint.
        var joinColumn = ParkingLot.class
                .getDeclaredField("operator")
                .getAnnotation(jakarta.persistence.JoinColumn.class);
        assertNotNull(joinColumn, "@JoinColumn must be present on the operator field");
        assertFalse(joinColumn.nullable(),
                "operator_id must be NOT NULL in the DB schema; nullable must be false");
    }

    // -------------------------------------------------------------------------
    // Spot management
    // -------------------------------------------------------------------------

    @Test
    void addSpotSyncsBothSides() {
        ParkingLot lot = new ParkingLot();
        ParkingSpot spot = new ParkingSpot();
        lot.addSpot(spot);
        assertTrue(lot.getSpots().contains(spot));
        assertSame(lot, spot.getParkingLot());
    }

    @Test
    void deactivateSpotSetsMaintenanceAndKeepsAssociation() {
        ParkingLot lot = new ParkingLot();
        ParkingSpot spot = new ParkingSpot();
        lot.addSpot(spot);
        lot.deactivateSpot(spot);
        assertTrue(lot.getSpots().contains(spot));
        assertSame(lot, spot.getParkingLot());
        assertEquals(ParkingSpot.SpotStatus.MAINTENANCE, spot.getStatus());
    }

    @Test
    void deactivateSpotThrowsWhenSpotNotInLot() {
        ParkingLot lot = new ParkingLot();
        ParkingSpot spot = new ParkingSpot();
        assertThrows(IllegalArgumentException.class, () -> lot.deactivateSpot(spot));
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    @Test
    void entityHasCorrectTableName() {
        var table = ParkingLot.class.getAnnotation(jakarta.persistence.Table.class);
        assertNotNull(table);
        assertEquals("parking_lots", table.name());
    }

    @Test
    void statusEnumContainsExpectedValues() {
        assertEquals(2, ParkingLot.Status.values().length);
        assertNotNull(ParkingLot.Status.ACTIVE);
        assertNotNull(ParkingLot.Status.INACTIVE);
    }
}
