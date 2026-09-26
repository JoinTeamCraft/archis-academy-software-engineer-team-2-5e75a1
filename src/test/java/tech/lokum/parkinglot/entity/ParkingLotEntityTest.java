package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParkingLotEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        ParkingLot lot = new ParkingLot();
        lot.setId(1L);
        lot.setName("Downtown Lot");
        lot.setAddress("123 Main St");
        lot.setStatus(ParkingLot.Status.ACTIVE);
        lot.setOperator(new User());

        assertEquals(1L, lot.getId());
        assertEquals("Downtown Lot", lot.getName());
        assertEquals("123 Main St", lot.getAddress());
        assertEquals(ParkingLot.Status.ACTIVE, lot.getStatus());
        assertNotNull(lot.getOperator());
    }

    @Test
    void addSpotSyncsBothSides() {
        ParkingLot lot = new ParkingLot();
        ParkingSpot spot = new ParkingSpot();
        lot.addSpot(spot);
        assertTrue(lot.getSpots().contains(spot));
        assertSame(lot, spot.getParkingLot());
    }

    @Test
    void removeSpotClearsOwningSide() {
        ParkingLot lot = new ParkingLot();
        ParkingSpot spot = new ParkingSpot();
        lot.addSpot(spot);
        lot.removeSpot(spot);
        assertFalse(lot.getSpots().contains(spot));
        assertNull(spot.getParkingLot());
    }

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
