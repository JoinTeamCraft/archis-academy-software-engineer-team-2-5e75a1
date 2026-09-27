package tech.lokum.parkinglot.repository;

import tech.lokum.parkinglot.entity.Vehicle;
import tech.lokum.parkinglot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByUser(User user);
}
