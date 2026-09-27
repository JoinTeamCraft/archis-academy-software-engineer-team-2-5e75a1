package tech.lokum.parkinglot.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void mapsResourceNotFoundToNotFoundResponse() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Parking spot 42 was not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void mapsCustomValidationExceptionToBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Start time must be before end time"))
                .andExpect(jsonPath("$.path").value("/test/invalid"));
    }

    @Test
    void mapsActiveParkingLotUniqueConstraintToConflict() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("An active parking lot already exists with this name and location"))
                .andExpect(jsonPath("$.path").value("/test/duplicate"));
    }

    @Test
    void mapsBeanValidationErrorsToBadRequest() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("name: must not be blank"))
                .andExpect(jsonPath("$.path").value("/test/validated"));
    }

    @Test
    void mapsMalformedJsonToBadRequest() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"))
                .andExpect(jsonPath("$.path").value("/test/validated"));
    }

    @Test
    void hidesUnexpectedExceptionDetailsFromResponse() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/test/unexpected"));
    }

    @RestController
    static class ExceptionTestController {

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Parking spot 42 was not found");
        }

        @GetMapping("/test/invalid")
        void invalid() {
            throw new ValidationException("Start time must be before end time");
        }

        @GetMapping("/test/duplicate")
        void duplicate() {
            var constraintViolation = new org.hibernate.exception.ConstraintViolationException(
                    "Duplicate parking lot",
                    new SQLException("Unique constraint violation"),
                    "uk_parking_lot_name_address_status"
            );
            throw new DataIntegrityViolationException("Duplicate parking lot", constraintViolation);
        }

        @PostMapping("/test/validated")
        void validated(@Valid @RequestBody ValidatedRequest request) {
            // Intentionally empty: request binding validates the body before invoking this method.
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("Database password must not be exposed");
        }
    }

    record ValidatedRequest(@NotBlank String name) {
    }
}
