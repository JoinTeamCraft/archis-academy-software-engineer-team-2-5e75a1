package tech.lokum.parkinglot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.lokum.parkinglot.entity.*;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VehicleRepositoryTest {

    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByLicensePlate_ShouldReturnVehicle_WhenPlateExists() {
        User customer = createUser("Cust");
        Vehicle vehicle = new Vehicle();
        vehicle.setUser(customer);
        vehicle.setLicensePlate("PLT-" + UUID.randomUUID());
        vehicle.setType(Vehicle.VehicleType.CAR);
        vehicle.setColor("Red");
        vehicleRepository.save(vehicle);

        Optional<Vehicle> found = vehicleRepository.findByLicensePlate(vehicle.getLicensePlate());
        assertThat(found).isPresent();
        assertThat(found.get().getLicensePlate()).isEqualTo(vehicle.getLicensePlate());
    }

    @Test
    void findByLicensePlate_ShouldReturnEmpty_WhenPlateDoesNotExist() {
        assertThat(vehicleRepository.findByLicensePlate("XYZ-9999")).isEmpty();
    }

    @Test
    void findByUser_ShouldReturnVehicles_ForGivenUser() {
        User customer = createUser("Cust");
        Vehicle v1 = new Vehicle();
        v1.setUser(customer);
        v1.setLicensePlate("PLT-" + UUID.randomUUID());
        v1.setType(Vehicle.VehicleType.CAR);
        Vehicle v2 = new Vehicle();
        v2.setUser(customer);
        v2.setLicensePlate("PLT-" + UUID.randomUUID());
        v2.setType(Vehicle.VehicleType.MOTORBIKE);
        vehicleRepository.saveAll(List.of(v1, v2));

        List<Vehicle> found = vehicleRepository.findByUser(customer);
        assertThat(found).hasSize(2);
    }

    private User createUser(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name + "_" + UUID.randomUUID() + "@test.com");
        user.setPasswordHash("hashed");
        user.setRole(User.Role.CUSTOMER);
        return userRepository.save(user);
    }
}
