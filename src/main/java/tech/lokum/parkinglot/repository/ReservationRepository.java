package tech.lokum.parkinglot.repository;

import tech.lokum.parkinglot.entity.Reservation;
import tech.lokum.parkinglot.entity.ParkingSpot;
import tech.lokum.parkinglot.entity.Vehicle;
import tech.lokum.parkinglot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByParkingSpotAndStatus(ParkingSpot parkingSpot, Reservation.Status status);
    List<Reservation> findByVehicleAndStatus(Vehicle vehicle, Reservation.Status status);
    List<Reservation> findByCustomerAndStatus(User customer, Reservation.Status status);

    /**
     * Detects time-window overlap for a spot across active statuses (e.g. PENDING, CONFIRMED).
     * Excludes CANCELLED, EXPIRED, and COMPLETED at the call site via {@code activeStatuses}.
     */
    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.parkingSpot = :spot
            AND r.status IN :activeStatuses
            AND r.startTime < :endTime
            AND r.endTime > :startTime
            """)
    boolean existsOverlappingActiveReservation(
            @Param("spot") ParkingSpot spot,
            @Param("activeStatuses") Collection<Reservation.Status> activeStatuses,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
