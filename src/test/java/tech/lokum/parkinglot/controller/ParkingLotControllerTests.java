package tech.lokum.parkinglot.controller;

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

    @Test
    void createsParkingLotAndReturnsCreatedResource() throws Exception {
        User operator = saveOperator("create-lot@example.com", User.Role.OPERATOR);
        long countBefore = parkingLotRepository.count();
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(operator.getId(), "Central Garage", "123 Main St", 120)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Central Garage"))
                .andExpect(jsonPath("$.location").value("123 Main St"))
                .andExpect(jsonPath("$.capacity").value(120));
        assertEquals(countBefore + 1, parkingLotRepository.count());
        ParkingLot createdLot = parkingLotRepository.findAll().stream()
                .filter(lot -> "Central Garage".equals(lot.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(operator.getId(), createdLot.getOperator().getId());
    }

    @Test
    void rejectsInvalidParkingLotWithBadRequest() throws Exception {
        User operator = saveOperator("invalid-lot@example.com", User.Role.OPERATOR);
        long countBefore = parkingLotRepository.count();
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(operator.getId(), " ", "", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name:")))
                .andExpect(jsonPath("$.message", containsString("location:")))
                .andExpect(jsonPath("$.message", containsString("capacity:")));
        assertEquals(countBefore, parkingLotRepository.count());
    }

    @Test
    void rejectsDuplicateActiveParkingLotAfterTrimmingInput() throws Exception {
        User operator = saveOperator("duplicate-lot@example.com", User.Role.OPERATOR);
        String body = createRequest(operator.getId(), "Duplicate Garage", "50 Main St", 20);
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
        long countAfterFirstCreate = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(operator.getId(), " Duplicate Garage ", " 50 Main St ", 20)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));

        assertEquals(countAfterFirstCreate, parkingLotRepository.count());
    }

    @Test
    void rejectsCreatingLotWhenMatchingInactiveLotExistsBecauseNameAndLocationMustBeUnique() throws Exception {
        User operator = saveOperator("inactive-lot@example.com", User.Role.OPERATOR);
        ParkingLot inactiveLot = new ParkingLot();
        inactiveLot.setName("Inactive Garage");
        inactiveLot.setLocation("60 Main St");
        inactiveLot.setCapacity(20);
        inactiveLot.setStatus(ParkingLot.Status.INACTIVE);
        operator.addParkingLot(inactiveLot);
        parkingLotRepository.saveAndFlush(inactiveLot);

        long countBeforeCreate = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(operator.getId(), "Inactive Garage", "60 Main St", 20)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));

        assertEquals(countBeforeCreate, parkingLotRepository.count());
    }

    @Test
    void databaseConstraintPreventsDuplicateNameAndLocationRegardlessOfStatus() {
        User operator = saveOperator("constraint-lot@example.com", User.Role.OPERATOR);
        saveLot(operator, "Constraint Garage", "70 Main St", ParkingLot.Status.INACTIVE);
        ParkingLot duplicateLot = createLot(operator, "Constraint Garage", "70 Main St", ParkingLot.Status.INACTIVE);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> parkingLotRepository.saveAndFlush(duplicateLot)
        );
    }

    private void saveLot(User operator, String name, String location, ParkingLot.Status status) {
        parkingLotRepository.saveAndFlush(createLot(operator, name, location, status));
    }

    private ParkingLot createLot(User operator, String name, String location, ParkingLot.Status status) {
        ParkingLot lot = new ParkingLot();
        lot.setName(name);
        lot.setLocation(location);
        lot.setCapacity(20);
        lot.setStatus(status);
        operator.addParkingLot(lot);
        return lot;
    }

    @Test
    void rejectsUnknownOperatorId() throws Exception {
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(Long.MAX_VALUE, "Missing Operator Lot", "90 Main St", 10)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Operator " + Long.MAX_VALUE + " was not found"));
    }

    @Test
    void rejectsUserThatIsNotAnOperator() throws Exception {
        User customer = saveOperator("customer-lot@example.com", User.Role.CUSTOMER);
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(customer.getId(), "Customer Lot", "91 Main St", 10)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only operators can create parking lots"));
    }

    @Test
    void requiresPositiveOperatorId() throws Exception {
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Missing Operator\",\"location\":\"92 Main St\",\"capacity\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("operatorId:")));

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(createRequest(0L, "Invalid Operator", "93 Main St", 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("operatorId: must be greater than 0")));
    }

    private User saveOperator(String email, User.Role role) {
        User operator = new User();
        operator.setName("Test User");
        operator.setEmail(email);
        operator.setPasswordHash("test-password-hash");
        operator.setRole(role);
        return userRepository.saveAndFlush(operator);
    }

    private String createRequest(Long operatorId, String name, String location, int capacity) {
        return "{\"operatorId\":" + operatorId + ",\"name\":\"" + name
                + "\",\"location\":\"" + location + "\",\"capacity\":" + capacity + "}";
    }

    @Test
    void publishesCreateEndpointInOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/parking-lots'].post.summary").value("Create a parking lot"))
                .andExpect(jsonPath("$.paths['/api/parking-lots'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/parking-lots'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/parking-lots'].post.responses['409']").exists());
    }
}
