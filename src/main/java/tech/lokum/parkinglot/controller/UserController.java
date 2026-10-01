package tech.lokum.parkinglot.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.lokum.parkinglot.dto.RegisterRequest;
import tech.lokum.parkinglot.dto.RegisterResponse;
import tech.lokum.parkinglot.service.UserService;

import java.net.URI;

/**
 * REST controller for user registration.
 *
 * <p>Handles {@code POST /api/users} to register a new user. Delegates to
 * {@link UserService} for business logic and returns the created user with
 * a {@code 201 Created} status and {@code Location} header.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<RegisterResponse> userRegister(
            @Valid @RequestBody RegisterRequest request
            ){
        RegisterResponse response = userService.registerUser(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
