package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@test.com");
        user.setPasswordHash("hashed");
        user.setRole(User.Role.CUSTOMER);

        assertEquals(1L, user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("hashed", user.getPasswordHash());
        assertEquals(User.Role.CUSTOMER, user.getRole());
    }

    @Test
    void addVehicleSyncsBothSides() {
        User user = new User();
        Vehicle vehicle = new Vehicle();
        user.addVehicle(vehicle);
        assertTrue(user.getVehicles().contains(vehicle));
        assertSame(user, vehicle.getUser());
    }

    @Test
    void addParkingLotSyncsBothSides() {
        User user = new User();
        ParkingLot lot = new ParkingLot();
        user.addParkingLot(lot);
        assertTrue(user.getParkingLots().contains(lot));
        assertSame(user, lot.getOperator());
    }

    @Test
    void addParkingLotThrowsWhenLotHasDifferentOperator() {
        User user = new User();
        User other = new User();
        ParkingLot lot = new ParkingLot();
        lot.setOperator(other);
        assertThrows(IllegalStateException.class, () -> user.addParkingLot(lot));
    }

    @Test
    void addParkingLotThrowsWhenLotAlreadyHasSameOperator() {
        User user = new User();
        ParkingLot lot = new ParkingLot();
        lot.setOperator(user);
        assertThrows(IllegalStateException.class, () -> user.addParkingLot(lot));
    }

    @Test
    void setPasswordHashStoresValueUnchanged() {
        User user = new User();
        user.setPasswordHash("bcrypt-hash-from-service");
        assertEquals("bcrypt-hash-from-service", user.getPasswordHash());
    }

    @Test
    void entityHasCorrectTableName() {
        var table = User.class.getAnnotation(jakarta.persistence.Table.class);
        assertNotNull(table);
        assertEquals("users", table.name());
    }

    @Test
    void roleEnumContainsExpectedValues() {
        assertEquals(3, User.Role.values().length);
        assertNotNull(User.Role.ADMIN);
        assertNotNull(User.Role.OPERATOR);
        assertNotNull(User.Role.CUSTOMER);
    }
}
