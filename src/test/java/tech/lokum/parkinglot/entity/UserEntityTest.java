package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        User user = new User();
        user.setId(1L);
        user.setName("Rahul");
        user.setEmail("rahul@test.com");
        user.setPassword("hashed");
        user.setRole(User.Role.CUSTOMER);

        assertEquals(1L, user.getId());
        assertEquals("Rahul", user.getName());
        assertEquals("rahul@test.com", user.getEmail());
        assertEquals(User.Role.CUSTOMER, user.getRole());
    }

    @Test
    void entityHasCorrectTableName() {
        assertEquals(User.class.getAnnotation(jakarta.persistence.Table.class).name(), "users");
    }

    @Test
    void roleEnumContainsExpectedValues() {
        assertEquals(3, User.Role.values().length);
        assertNotNull(User.Role.ADMIN);
        assertNotNull(User.Role.OPERATOR);
        assertNotNull(User.Role.CUSTOMER);
    }
}
