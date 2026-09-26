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
