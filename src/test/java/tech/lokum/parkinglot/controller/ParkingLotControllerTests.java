package tech.lokum.parkinglot.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tech.lokum.parkinglot.entity.ParkingLot;
import tech.lokum.parkinglot.entity.User;
import tech.lokum.parkinglot.repository.ParkingLotRepository;
import tech.lokum.parkinglot.repository.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParkingLotControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingLotRepository parkingLotRepository;

    @Autowired
    private UserRepository userRepository;

    // -------------------------------------------------------------------------
    // Create parking lot
    // -------------------------------------------------------------------------

    @Test
    void createsParkingLotAndReturnsCreatedResource() throws Exception {
        User operator = saveOperator(
                "create-lot@example.com",
                User.Role.OPERATOR
        );

        long countBefore = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                operator.getId(),
                                "Central Garage",
                                "123 Main St",
                                120
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Central Garage"))
                .andExpect(jsonPath("$.location").value("123 Main St"))
                .andExpect(jsonPath("$.capacity").value(120));

        assertEquals(
                countBefore + 1,
                parkingLotRepository.count()
        );

        ParkingLot createdLot = parkingLotRepository.findAll()
                .stream()
                .filter(lot -> "Central Garage".equals(lot.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(
                operator.getId(),
                createdLot.getOperator().getId()
        );

        assertEquals(
                ParkingLot.Status.ACTIVE,
                createdLot.getStatus()
        );
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    @Test
    void rejectsInvalidParkingLotWithBadRequest() throws Exception {
        User operator = saveOperator(
                "invalid-lot@example.com",
                User.Role.OPERATOR
        );

        long countBefore = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                operator.getId(),
                                " ",
                                "",
                                0
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name:")))
                .andExpect(jsonPath("$.message", containsString("location:")))
                .andExpect(jsonPath("$.message", containsString("capacity:")));

        assertEquals(
                countBefore,
                parkingLotRepository.count()
        );
    }

    @Test
    void requiresPositiveOperatorId() throws Exception {

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Missing Operator",
                                    "location": "92 Main St",
                                    "capacity": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("operatorId:")));

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                0L,
                                "Invalid Operator",
                                "93 Main St",
                                10
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(containsString("operatorId: must be greater than 0")));
    }

    // -------------------------------------------------------------------------
    // Active parking lot uniqueness
    // -------------------------------------------------------------------------

    @Test
    void rejectsDuplicateActiveParkingLotAfterTrimmingInput() throws Exception {
        User operator = saveOperator(
                "duplicate-lot@example.com",
                User.Role.OPERATOR
        );

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                operator.getId(),
                                "Duplicate Garage",
                                "50 Main St",
                                20
                        )))
                .andExpect(status().isCreated());

        long countAfterFirstCreate = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                operator.getId(),
                                " Duplicate Garage ",
                                " 50 Main St ",
                                20
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A parking lot already exists with this name and location"));

        assertEquals(
                countAfterFirstCreate,
                parkingLotRepository.count()
        );
    }

    @Test
    void rejectsDuplicateActiveParkingLotWithExactSameNameAndAddress()
            throws Exception {

        User operator = saveOperator(
                "duplicate-exact@example.com",
                User.Role.OPERATOR
        );

        saveLot(
                operator,
                "Central Parking",
                "100 Main St",
                ParkingLot.Status.ACTIVE
        );

        long countBefore = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                operator.getId(),
                                "Central Parking",
                                "100 Main St",
                                50
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A parking lot already exists with this name and location"));

        assertEquals(
                countBefore,
                parkingLotRepository.count()
        );
    }

    @Test
    void allowsActiveLotWhenMatchingLotIsInactive() throws Exception {
        User operator = saveOperator(
                "inactive-lot@example.com",
                User.Role.OPERATOR
        );

        saveLot(
                operator,
                "Inactive Garage",
                "60 Main St",
                ParkingLot.Status.INACTIVE
        );

        long countBefore = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                operator.getId(),
                                "Inactive Garage",
                                "60 Main St",
                                20
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Inactive Garage"))
                .andExpect(jsonPath("$.location").value("60 Main St"))
                .andExpect(jsonPath("$.capacity").value(20));

        assertEquals(
                countBefore + 1,
                parkingLotRepository.count()
        );

        long matchingActiveLots = parkingLotRepository.findAll()
                .stream()
                .filter(lot -> "Inactive Garage".equals(lot.getName()))
                .filter(lot -> "60 Main St".equals(lot.getAddress()))
                .filter(lot -> lot.getStatus() == ParkingLot.Status.ACTIVE)
                .count();

        assertEquals(1, matchingActiveLots);
    }

    @Test
    void allowsMultipleInactiveLotsWithSameNameAndAddress() {

        User operator = saveOperator(
                "multi-inactive-lot@example.com",
                User.Role.OPERATOR
        );

        saveLot(
                operator,
                "Retired Garage",
                "70 Main St",
                ParkingLot.Status.INACTIVE
        );

        saveLot(
                operator,
                "Retired Garage",
                "70 Main St",
                ParkingLot.Status.INACTIVE
        );

        long inactiveCount = parkingLotRepository.findAll()
                .stream()
                .filter(lot -> "Retired Garage".equals(lot.getName()))
                .filter(lot -> "70 Main St".equals(lot.getAddress()))
                .filter(lot -> lot.getStatus() == ParkingLot.Status.INACTIVE)
                .count();

        assertEquals(2, inactiveCount);
    }

    @Test
    void allowsInactiveLotWithSameNameAndAddressAsActiveLot() {

        User operator = saveOperator(
                "active-inactive-lot@example.com",
                User.Role.OPERATOR
        );

        saveLot(
                operator,
                "Mixed Status Garage",
                "75 Main St",
                ParkingLot.Status.ACTIVE
        );

        // This should be allowed because the partial unique index
        // only applies to ACTIVE rows.
        saveLot(
                operator,
                "Mixed Status Garage",
                "75 Main St",
                ParkingLot.Status.INACTIVE
        );

        long matchingLots = parkingLotRepository.findAll()
                .stream()
                .filter(lot -> "Mixed Status Garage".equals(lot.getName()))
                .filter(lot -> "75 Main St".equals(lot.getAddress()))
                .count();

        assertEquals(2, matchingLots);
    }

    // -------------------------------------------------------------------------
    // Database partial unique index
    // -------------------------------------------------------------------------

    /**
     * Verifies that the PostgreSQL partial unique index prevents duplicate
     * ACTIVE parking lots even when the application-level duplicate check
     * is bypassed.
     *
     * <p>This test is disabled for the H2 test profile because H2 does not
     * provide the same PostgreSQL partial-index behavior used by the
     * production database.
     *
     * <p>Run this test against PostgreSQL, preferably using Testcontainers.
     */
    @Test
    @Disabled(
            "Requires PostgreSQL because the production partial unique index "
                    + "uses WHERE status = 'ACTIVE'"
    )
    void databaseIndexPreventsDuplicateActiveNameAndAddressEvenIfAppCheckIsBypassed() {

        User operator = saveOperator(
                "constraint-lot@example.com",
                User.Role.OPERATOR
        );

        saveLot(
                operator,
                "Constraint Garage",
                "80 Main St",
                ParkingLot.Status.ACTIVE
        );

        ParkingLot duplicateActiveLot = createLot(
                operator,
                "Constraint Garage",
                "80 Main St",
                ParkingLot.Status.ACTIVE
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> parkingLotRepository.saveAndFlush(duplicateActiveLot)
        );
    }

    // -------------------------------------------------------------------------
    // Operator validation
    // -------------------------------------------------------------------------

    @Test
    void rejectsUnknownOperatorId() throws Exception {

        long unknownOperatorId = Long.MAX_VALUE;

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                unknownOperatorId,
                                "Missing Operator Lot",
                                "90 Main St",
                                10
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Operator " + unknownOperatorId + " was not found"));
    }

    @Test
    void rejectsUserThatIsNotAnOperator() throws Exception {

        User customer = saveOperator(
                "customer-lot@example.com",
                User.Role.CUSTOMER
        );

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(
                                customer.getId(),
                                "Customer Lot",
                                "91 Main St",
                                10
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Only operators can create parking lots"));
    }

    // -------------------------------------------------------------------------
    // Entity invariant
    // -------------------------------------------------------------------------

    @Test
    void setOperatorToNullThrowsImmediatelyBeforeAnyDatabaseCall() {

        ParkingLot lot = new ParkingLot();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> lot.setOperator(null)
        );

        assertEquals(
                "operator must not be null",
                exception.getMessage()
        );
    }

    // -------------------------------------------------------------------------
    // OpenAPI documentation
    // -------------------------------------------------------------------------

    @Test
    void publishesCreateEndpointInOpenApiDocumentation() throws Exception {

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/parking-lots'].post.summary"
                ).value("Create a parking lot"))
                .andExpect(jsonPath(
                        "$.paths['/api/parking-lots'].post.responses['201']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/parking-lots'].post.responses['400']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/parking-lots'].post.responses['403']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/parking-lots'].post.responses['404']"
                ).exists())
                .andExpect(jsonPath(
                        "$.paths['/api/parking-lots'].post.responses['409']"
                ).exists());
    }

    // -------------------------------------------------------------------------
    // Test helpers
    // -------------------------------------------------------------------------

    private User saveOperator(String email, User.Role role) {

        User user = new User();

        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash("test-password-hash");
        user.setRole(role);

        return userRepository.saveAndFlush(user);
    }

    private ParkingLot createLot(
            User operator,
            String name,
            String location,
            ParkingLot.Status status
    ) {

        ParkingLot lot = new ParkingLot();

        lot.setName(name);
        lot.setAddress(location);
        lot.setCapacity(20);
        lot.setStatus(status);

        operator.addParkingLot(lot);

        return lot;
    }

    private void saveLot(
            User operator,
            String name,
            String location,
            ParkingLot.Status status
    ) {

        parkingLotRepository.saveAndFlush(
                createLot(
                        operator,
                        name,
                        location,
                        status
                )
        );
    }

    private String createRequest(
            Long operatorId,
            String name,
            String location,
            int capacity
    ) {

        return """
                {
                    "operatorId": %d,
                    "name": "%s",
                    "location": "%s",
                    "capacity": %d
                }
                """.formatted(
                operatorId,
                name,
                location,
                capacity
        );
    }
}