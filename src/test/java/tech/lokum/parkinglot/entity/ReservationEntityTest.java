package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ReservationEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setVehicle(new Vehicle());
        reservation.setParkingSpot(new ParkingSpot());
        reservation.setCustomer(new User());
        reservation.setStartTime(LocalDateTime.of(2026, 9, 26, 10, 0));
        reservation.setEndTime(LocalDateTime.of(2026, 9, 26, 14, 0));
        reservation.setStatus(Reservation.Status.CONFIRMED);

        assertEquals(1L, reservation.getId());
        assertNotNull(reservation.getVehicle());
        assertNotNull(reservation.getParkingSpot());
        assertNotNull(reservation.getCustomer());
        assertEquals(Reservation.Status.CONFIRMED, reservation.getStatus());
    }

    @Test
    void entityHasCorrectTableName() {
        assertEquals(Reservation.class.getAnnotation(jakarta.persistence.Table.class).name(), "reservations");
    }

    @Test
    void statusEnumContainsExpectedValues() {
        assertEquals(4, Reservation.Status.values().length);
        assertNotNull(Reservation.Status.CONFIRMED);
        assertNotNull(Reservation.Status.CANCELLED);
        assertNotNull(Reservation.Status.EXPIRED);
        assertNotNull(Reservation.Status.COMPLETED);
    }
}
