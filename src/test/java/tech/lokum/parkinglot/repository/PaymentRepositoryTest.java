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
class PaymentRepositoryTest {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ParkingSpotRepository parkingSpotRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ParkingLotRepository parkingLotRepository;

    @Test
    void findByReservation_ShouldReturnPayment_WhenExists() {
        Reservation reservation = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation);

        Payment payment = createPayment(reservation);
        paymentRepository.save(payment);

        Optional<Payment> found = paymentRepository.findByReservation(reservation);
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void findByReservation_ShouldReturnEmpty_WhenNoPayment() {
        Reservation reservation = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation);

        assertThat(paymentRepository.findByReservation(reservation)).isEmpty();
    }

    @Test
    void findByStatus_ShouldReturnPayments_WhenStatusMatches() {
        Reservation reservation1 = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation1);
        Reservation reservation2 = createReservation(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation2);

        Payment paid = createPayment(reservation1);
        paid.setStatus(Payment.Status.PAID);
        Payment pending = createPayment(reservation2);
        pending.setStatus(Payment.Status.PENDING);
        paymentRepository.saveAll(List.of(paid, pending));

        List<Payment> paidPayments = paymentRepository.findByStatus(Payment.Status.PAID);
        assertThat(paidPayments).hasSize(1);
        assertThat(paidPayments.get(0).getStatus()).isEqualTo(Payment.Status.PAID);
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

    private Payment createPayment(Reservation reservation) {
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("10.00"));
        payment.setCurrency(Payment.Currency.INR);
        payment.setMethod(Payment.PaymentMethod.CARD);
        payment.setStatus(Payment.Status.PENDING);
        reservation.setPayment(payment);
        return payment;
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
