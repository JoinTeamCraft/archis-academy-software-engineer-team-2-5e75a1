package tech.lokum.parkinglot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import tech.lokum.parkinglot.entity.*;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ParkingSpotRepository}.
 *
 * <p>Uses {@code @DataJpaTest} to load only the JPA layer (repositories and entities)
 * with an embedded H2 database. Each test runs in a transaction that is rolled back
 * after completion, ensuring no state leaks between tests.
 */
@DataJpaTest
class ParkingSpotRepositoryTest {

    @Autowired private ParkingSpotRepository parkingSpotRepository;
    @Autowired private ParkingLotRepository parkingLotRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByParkingLot_ShouldReturnSpots_ForGivenLot() {
        User operator = createOperator();
        ParkingLot lot = createParkingLot(operator);
        parkingLotRepository.saveAndFlush(lot);

        ParkingSpot spot1 = createSpot(lot, ParkingSpot.SpotType.CAR, ParkingSpot.SpotStatus.AVAILABLE);
        ParkingSpot spot2 = createSpot(lot, ParkingSpot.SpotType.MOTORBIKE, ParkingSpot.SpotStatus.OCCUPIED);
        parkingSpotRepository.saveAll(List.of(spot1, spot2));

        List<ParkingSpot> found = parkingSpotRepository.findByParkingLot(lot);
        assertThat(found).hasSize(2);
    }

    @Test
    void findByParkingLotAndStatus_ShouldReturnFilteredSpots() {
        User operator = createOperator();
        ParkingLot lot = createParkingLot(operator);
        parkingLotRepository.saveAndFlush(lot);

        ParkingSpot available = createSpot(lot, ParkingSpot.SpotType.CAR, ParkingSpot.SpotStatus.AVAILABLE);
        ParkingSpot occupied = createSpot(lot, ParkingSpot.SpotType.CAR, ParkingSpot.SpotStatus.OCCUPIED);
        parkingSpotRepository.saveAll(List.of(available, occupied));

        List<ParkingSpot> found = parkingSpotRepository.findByParkingLotAndStatus(lot, ParkingSpot.SpotStatus.AVAILABLE);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(ParkingSpot.SpotStatus.AVAILABLE);
    }

    @Test
    void findByParkingLotAndStatusAndType_ShouldReturnExactMatch() {
        User operator = createOperator();
        ParkingLot lot = createParkingLot(operator);
        parkingLotRepository.saveAndFlush(lot);

        ParkingSpot carAvail = createSpot(lot, ParkingSpot.SpotType.CAR, ParkingSpot.SpotStatus.AVAILABLE);
        ParkingSpot motAvail = createSpot(lot, ParkingSpot.SpotType.MOTORBIKE, ParkingSpot.SpotStatus.AVAILABLE);
        parkingSpotRepository.saveAll(List.of(carAvail, motAvail));

        List<ParkingSpot> found = parkingSpotRepository.findByParkingLotAndStatusAndType(
                lot, ParkingSpot.SpotStatus.AVAILABLE, ParkingSpot.SpotType.CAR);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getType()).isEqualTo(ParkingSpot.SpotType.CAR);
    }

    private User createOperator() {
        User user = new User();
        user.setName("Op");
        user.setEmail("op_" + UUID.randomUUID() + "@test.com");
        user.setPasswordHash("hashed");
        user.setRole(User.Role.OPERATOR);
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

    private ParkingSpot createSpot(ParkingLot lot, ParkingSpot.SpotType type, ParkingSpot.SpotStatus status) {
        ParkingSpot spot = new ParkingSpot();
        spot.setParkingLot(lot);
        spot.setSpotNumber(UUID.randomUUID().toString().substring(0, 8));
        spot.setType(type);
        spot.setStatus(status);
        return spot;
    }
}
