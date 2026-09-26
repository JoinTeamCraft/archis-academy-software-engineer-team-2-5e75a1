package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParkingSpotEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        ParkingSpot spot = new ParkingSpot();
        spot.setId(1L);
        spot.setParkingLot(new ParkingLot());
        spot.setSpotNumber("A-01");
        spot.setType(ParkingSpot.SpotType.CAR);
        spot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);

        assertEquals(1L, spot.getId());
        assertNotNull(spot.getParkingLot());
        assertEquals("A-01", spot.getSpotNumber());
        assertEquals(ParkingSpot.SpotType.CAR, spot.getType());
        assertEquals(ParkingSpot.SpotStatus.AVAILABLE, spot.getStatus());
    }

    @Test
    void entityHasCorrectTableName() {
        assertEquals(ParkingSpot.class.getAnnotation(jakarta.persistence.Table.class).name(), "parking_spots");
    }

    @Test
    void spotTypeEnumContainsExpectedValues() {
        assertEquals(4, ParkingSpot.SpotType.values().length);
        assertNotNull(ParkingSpot.SpotType.CAR);
        assertNotNull(ParkingSpot.SpotType.MOTORBIKE);
        assertNotNull(ParkingSpot.SpotType.TRUCK);
        assertNotNull(ParkingSpot.SpotType.EV);
    }

    @Test
    void spotStatusEnumContainsExpectedValues() {
        assertEquals(3, ParkingSpot.SpotStatus.values().length);
        assertNotNull(ParkingSpot.SpotStatus.AVAILABLE);
        assertNotNull(ParkingSpot.SpotStatus.OCCUPIED);
        assertNotNull(ParkingSpot.SpotStatus.MAINTENANCE);
    }
}
