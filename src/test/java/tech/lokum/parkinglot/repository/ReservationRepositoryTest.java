package tech.lokum.parkinglot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.lokum.parkinglot.entity.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationRepositoryTest {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ParkingSpotRepository parkingSpotRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ParkingLotRepository parkingLotRepository;

    @Test
    void findByParkingSpotAndStatus_ShouldReturnReservations() {
        Reservation reservation = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation);

        List<Reservation> found = reservationRepository.findByParkingSpotAndStatus(
                reservation.getParkingSpot(), Reservation.Status.CONFIRMED);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(Reservation.Status.CONFIRMED);
    }

    @Test
    void findByVehicleAndStatus_ShouldReturnReservations() {
        Reservation reservation = createReservation(Reservation.Status.PENDING);
        reservationRepository.save(reservation);

        List<Reservation> found = reservationRepository.findByVehicleAndStatus(
                reservation.getVehicle(), Reservation.Status.PENDING);
        assertThat(found).hasSize(1);
    }

    @Test
    void findByCustomerAndStatus_ShouldReturnReservations() {
        Reservation reservation = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation);

        List<Reservation> found = reservationRepository.findByCustomerAndStatus(
                reservation.getCustomer(), Reservation.Status.CONFIRMED);
        assertThat(found).hasSize(1);
    }

    @Test
    void existsOverlappingActiveReservation_ShouldReturnTrue_WhenOverlapExists() {
        Reservation existing = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(existing);

        LocalDateTime overlapStart = existing.getStartTime().plusMinutes(30);
        LocalDateTime overlapEnd = existing.getEndTime().plusMinutes(30);

        boolean overlap = reservationRepository.existsOverlappingActiveReservation(
                existing.getParkingSpot(),
                List.of(Reservation.Status.CONFIRMED, Reservation.Status.PENDING),
                overlapStart, overlapEnd);
        assertThat(overlap).isTrue();
    }

    @Test
    void existsOverlappingActiveReservation_ShouldReturnFalse_WhenNoOverlap() {
        Reservation existing = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(existing);

        LocalDateTime afterEnd = existing.getEndTime().plusHours(1);
        LocalDateTime farEnd = afterEnd.plusHours(1);

        boolean overlap = reservationRepository.existsOverlappingActiveReservation(
                existing.getParkingSpot(),
                List.of(Reservation.Status.CONFIRMED, Reservation.Status.PENDING),
                afterEnd, farEnd);
        assertThat(overlap).isFalse();
    }

    @Test
    void existsOverlappingActiveReservation_ShouldReturnFalse_ForCancelledStatus() {
        Reservation cancelled = createReservation(Reservation.Status.CANCELLED);
        reservationRepository.save(cancelled);

        LocalDateTime withinRange = cancelled.getStartTime().plusMinutes(30);
        LocalDateTime farEnd = cancelled.getEndTime().plusMinutes(30);

        boolean overlap = reservationRepository.existsOverlappingActiveReservation(
                cancelled.getParkingSpot(),
                List.of(Reservation.Status.CONFIRMED, Reservation.Status.PENDING),
                withinRange, farEnd);
        assertThat(overlap).isFalse();
    }

    private Reservation createReservation(Reservation.Status status) {
        User operator = createUser("Op");
        ParkingLot lot = createParkingLot(operator);
        parkingLotRepository.save(lot);
        ParkingSpot spot = createParkingSpot(lot);
        parkingSpotRepository.save(spot);

        User customer = createUser("Cust");
        Vehicle vehicle = new Vehicle();
        vehicle.setUser(customer);
        vehicle.setLicensePlate("PLT-" + UUID.randomUUID());
        vehicle.setType(Vehicle.VehicleType.CAR);
        vehicleRepository.save(vehicle);

        Reservation reservation = new Reservation();
        reservation.setParkingSpot(spot);
        reservation.setVehicle(vehicle);
        reservation.setCustomer(customer);
        reservation.setStatus(status);
        reservation.setStartTime(LocalDateTime.now().plusHours(1));
        reservation.setEndTime(LocalDateTime.now().plusHours(2));
        return reservation;
    }

    private User createUser(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name + "_" + UUID.randomUUID() + "@test.com");
        user.setPasswordHash("hashed");
        user.setRole(name.equals("Op") ? User.Role.OPERATOR : User.Role.CUSTOMER);
        return userRepository.save(user);
    }

    private ParkingLot createParkingLot(User operator) {
        ParkingLot lot = new ParkingLot();
        lot.setName("Lot_" + UUID.randomUUID());
        lot.setAddress("123 Main St");
        lot.setStatus(ParkingLot.Status.ACTIVE);
        lot.setOperator(operator);
        return lot;
    }

    private ParkingSpot createParkingSpot(ParkingLot lot) {
        ParkingSpot spot = new ParkingSpot();
        spot.setParkingLot(lot);
        spot.setSpotNumber(UUID.randomUUID().toString().substring(0, 8));
        spot.setType(ParkingSpot.SpotType.CAR);
        spot.setStatus(ParkingSpot.SpotStatus.AVAILABLE);
        return spot;
    }
}
