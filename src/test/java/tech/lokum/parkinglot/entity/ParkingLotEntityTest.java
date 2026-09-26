package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParkingLotEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        ParkingLot lot = new ParkingLot();
        lot.setId(1L);
        lot.setName("Parking test Lot");
        lot.setAddress("123 test St");
        lot.setStatus(ParkingLot.Status.ACTIVE);
        lot.setOperator(new User());

        assertEquals(1L, lot.getId());
        assertEquals("Parking test Lot", lot.getName());
        assertEquals("123 test St", lot.getAddress());
        assertEquals(ParkingLot.Status.ACTIVE, lot.getStatus());
        assertNotNull(lot.getOperator());
    }

    @Test
    void entityHasCorrectTableName() {
        assertEquals(ParkingLot.class.getAnnotation(jakarta.persistence.Table.class).name(), "parking_lots");
    }

    @Test
    void statusEnumContainsExpectedValues() {
        assertEquals(2, ParkingLot.Status.values().length);
        assertNotNull(ParkingLot.Status.ACTIVE);
        assertNotNull(ParkingLot.Status.INACTIVE);
    }
}
