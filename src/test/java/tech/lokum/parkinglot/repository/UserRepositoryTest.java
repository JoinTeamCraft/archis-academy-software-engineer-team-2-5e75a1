package tech.lokum.parkinglot.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.lokum.parkinglot.entity.User;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        String email = "user_" + UUID.randomUUID() + "@test.com";
        User user = createUser(email);
        userRepository.save(user);

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
        userRepository.save(user);

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
