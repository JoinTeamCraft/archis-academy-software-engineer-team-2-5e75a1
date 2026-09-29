package tech.lokum.parkinglot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import tech.lokum.parkinglot.entity.*;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ParkingLotRepository}.
 *
 * <p>Uses {@code @DataJpaTest} to load only the JPA layer (repositories and entities)
 * with an embedded H2 database. Each test runs in a transaction that is rolled back
 * after completion, ensuring no state leaks between tests.
 */
@DataJpaTest
class ParkingLotRepositoryTest {

    @Autowired private ParkingLotRepository parkingLotRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByStatus_ShouldReturnLots_WhenStatusMatches() {
        User operator = createOperator();
        String name = "Lot_" + UUID.randomUUID();

        ParkingLot activeLot = new ParkingLot();
        activeLot.setName(name + "_active");
        activeLot.setAddress("123 Main St");
        activeLot.setStatus(ParkingLot.Status.ACTIVE);
        activeLot.setOperator(operator);

        ParkingLot inactiveLot = new ParkingLot();
        inactiveLot.setName(name + "_inactive");
        inactiveLot.setAddress("456 Oak Ave");
        inactiveLot.setStatus(ParkingLot.Status.INACTIVE);
        inactiveLot.setOperator(operator);

        parkingLotRepository.saveAll(List.of(activeLot, inactiveLot));
        parkingLotRepository.flush();

        List<ParkingLot> activeLots = parkingLotRepository.findByStatus(ParkingLot.Status.ACTIVE);
        assertThat(activeLots).hasSize(1);
        assertThat(activeLots.get(0).getStatus()).isEqualTo(ParkingLot.Status.ACTIVE);
    }

    @Test
    void findByOperator_ShouldReturnLots_WhenOperatorMatches() {
        User operator = createOperator();
        String name = "Lot_" + UUID.randomUUID();

        ParkingLot lot = new ParkingLot();
        lot.setName(name);
        lot.setAddress("123 Main St");
        lot.setStatus(ParkingLot.Status.ACTIVE);
        lot.setOperator(operator);
        parkingLotRepository.save(lot);

        List<ParkingLot> found = parkingLotRepository.findByOperator(operator);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getOperator().getId()).isEqualTo(operator.getId());
    }

    private User createOperator() {
        User user = new User();
        user.setName("Op");
        user.setEmail("op_" + UUID.randomUUID() + "@test.com");
        user.setPasswordHash("hashed");
        user.setRole(User.Role.OPERATOR);
        return userRepository.save(user);
    }
}
