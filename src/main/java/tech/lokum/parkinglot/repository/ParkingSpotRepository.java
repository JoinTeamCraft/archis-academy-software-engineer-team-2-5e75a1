package tech.lokum.parkinglot.repository;

import tech.lokum.parkinglot.entity.ParkingSpot;
import tech.lokum.parkinglot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByParkingLot(ParkingLot parkingLot);
    List<ParkingSpot> findByParkingLotAndStatus(ParkingLot parkingLot, ParkingSpot.SpotStatus status);
    List<ParkingSpot> findByParkingLotAndStatusAndType(ParkingLot parkingLot, ParkingSpot.SpotStatus status, ParkingSpot.SpotType type);
}
