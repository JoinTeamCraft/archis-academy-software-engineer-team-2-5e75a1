package tech.lokum.parkinglot.repository;

import tech.lokum.parkinglot.entity.Payment;
import tech.lokum.parkinglot.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByReservation(Reservation reservation);
    List<Payment> findByStatus(Payment.Status status);
}
