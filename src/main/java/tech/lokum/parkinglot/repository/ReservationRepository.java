package tech.lokum.parkinglot.repository;

import tech.lokum.parkinglot.entity.Reservation;
import tech.lokum.parkinglot.entity.ParkingSpot;
import tech.lokum.parkinglot.entity.Vehicle;
import tech.lokum.parkinglot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByParkingSpotAndStatus(ParkingSpot parkingSpot, Reservation.Status status);
    List<Reservation> findByVehicleAndStatus(Vehicle vehicle, Reservation.Status status);
    List<Reservation> findByCustomerAndStatus(User customer, Reservation.Status status);
}
