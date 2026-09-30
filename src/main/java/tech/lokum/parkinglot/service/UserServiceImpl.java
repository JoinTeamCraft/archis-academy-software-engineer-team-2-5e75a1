package tech.lokum.parkinglot.service;


import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.lokum.parkinglot.dto.RegisterRequest;
import tech.lokum.parkinglot.dto.RegisterResponse;
import tech.lokum.parkinglot.entity.User;
import tech.lokum.parkinglot.exception.ValidationException;
import tech.lokum.parkinglot.repository.UserRepository;

/**
 * Service for user registration.
 *
 * <p>Handles business logic for registering a new user: validates email uniqueness,
 * hashes the password with {@link PasswordEncoder}, and persists the user.
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("User email already exists");
        }

        User user = new User();
        user.setName(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(User.Role.valueOf(request.role()));

        User saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name());
    }
}
