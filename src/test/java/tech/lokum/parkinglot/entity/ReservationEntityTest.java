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
        var table = Reservation.class.getAnnotation(jakarta.persistence.Table.class);
        assertNotNull(table);
        assertEquals("reservations", table.name());
    }

    @Test
    void statusEnumContainsExpectedValues() {
        assertEquals(5, Reservation.Status.values().length);
        assertNotNull(Reservation.Status.PENDING);
        assertNotNull(Reservation.Status.CONFIRMED);
        assertNotNull(Reservation.Status.CANCELLED);
        assertNotNull(Reservation.Status.EXPIRED);
        assertNotNull(Reservation.Status.COMPLETED);
    }

    @Test
    void setPaymentSyncsBothSides() {
        Reservation reservation = new Reservation();
        Payment payment = new Payment();
        reservation.setPayment(payment);
        assertSame(payment, reservation.getPayment());
        assertSame(reservation, payment.getReservation());
    }

    @Test
    void setPaymentClearsPreviousAssociation() {
        Reservation reservation = new Reservation();
        Payment first = new Payment();
        Payment second = new Payment();
        reservation.setPayment(first);
        reservation.setPayment(second);
        assertSame(second, reservation.getPayment());
        assertNull(first.getReservation());
        assertSame(reservation, second.getReservation());
    }

}
