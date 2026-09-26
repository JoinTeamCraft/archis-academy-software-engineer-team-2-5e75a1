package tech.lokum.parkinglot.repository;

import tech.lokum.parkinglot.entity.ParkingLot;
import tech.lokum.parkinglot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findByStatus(ParkingLot.Status status);
    List<ParkingLot> findByOperator(User operator);
}
