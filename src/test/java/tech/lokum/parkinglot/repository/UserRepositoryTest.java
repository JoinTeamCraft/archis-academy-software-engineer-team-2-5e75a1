package tech.lokum.parkinglot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import tech.lokum.parkinglot.entity.*;
import java.util.UUID;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserRepository}.
 *
 * <p>Uses {@code @DataJpaTest} to load only the JPA layer (repositories and entities)
 * with an embedded H2 database. Each test runs in a transaction that is rolled back
 * after completion, ensuring no state leaks between tests.
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        String email = "user_" + UUID.randomUUID() + "@test.com";
        User user = createUser(email);
        userRepository.saveAndFlush(user);

        Optional<User> found = userRepository.findByEmail(email);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        assertThat(userRepository.findByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        String email = "user_" + UUID.randomUUID() + "@test.com";
        User user = createUser(email);
        userRepository.saveAndFlush(user);

        assertThat(userRepository.existsByEmail(email)).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setRole(User.Role.CUSTOMER);
        return user;
    }
}
