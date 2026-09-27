package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setUser(new User());
        vehicle.setLicensePlate("ABC-1234");
        vehicle.setType(Vehicle.VehicleType.CAR);
        vehicle.setColor("Red");

        assertEquals(1L, vehicle.getId());
        assertNotNull(vehicle.getUser());
        assertEquals("ABC-1234", vehicle.getLicensePlate());
        assertEquals(Vehicle.VehicleType.CAR, vehicle.getType());
        assertEquals("Red", vehicle.getColor());
    }

    @Test
    void entityHasCorrectTableName() {
        var table = Vehicle.class.getAnnotation(jakarta.persistence.Table.class);
        assertNotNull(table);
        assertEquals("vehicles", table.name());
    }

    @Test
    void vehicleTypeEnumContainsExpectedValues() {
        assertEquals(4, Vehicle.VehicleType.values().length);
        assertNotNull(Vehicle.VehicleType.CAR);
        assertNotNull(Vehicle.VehicleType.MOTORBIKE);
        assertNotNull(Vehicle.VehicleType.TRUCK);
        assertNotNull(Vehicle.VehicleType.EV);
    }
}
