package tech.lokum.parkinglot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tech.lokum.parkinglot.entity.ParkingLot;
import tech.lokum.parkinglot.repository.ParkingLotRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void createsParkingLotAndReturnsCreatedResource() throws Exception {
        long countBefore = parkingLotRepository.count();
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Central Garage\",\"location\":\"123 Main St\",\"capacity\":120}"))
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
        assertNull(createdLot.getOperator());
    }

    @Test
    void rejectsInvalidParkingLotWithBadRequest() throws Exception {
        long countBefore = parkingLotRepository.count();
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\" \",\"location\":\"\",\"capacity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name:")))
                .andExpect(jsonPath("$.message", containsString("location:")))
                .andExpect(jsonPath("$.message", containsString("capacity:")));
        assertEquals(countBefore, parkingLotRepository.count());
    }

    @Test
    void rejectsDuplicateActiveParkingLotAfterTrimmingInput() throws Exception {
        String body = "{\"name\":\"Duplicate Garage\",\"location\":\"50 Main St\",\"capacity\":20}";
        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
        long countAfterFirstCreate = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\" Duplicate Garage \",\"location\":\" 50 Main St \",\"capacity\":20}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("active parking lot already exists")));

        assertEquals(countAfterFirstCreate, parkingLotRepository.count());
    }

    @Test
    void allowsCreatingActiveParkingLotWhenMatchingLotIsInactive() throws Exception {
        ParkingLot inactiveLot = new ParkingLot();
        inactiveLot.setName("Inactive Garage");
        inactiveLot.setLocation("60 Main St");
        inactiveLot.setCapacity(20);
        inactiveLot.setStatus(ParkingLot.Status.INACTIVE);
        parkingLotRepository.saveAndFlush(inactiveLot);

        long countBeforeCreate = parkingLotRepository.count();

        mockMvc.perform(post("/api/parking-lots")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Inactive Garage\",\"location\":\"60 Main St\",\"capacity\":20}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Inactive Garage"))
                .andExpect(jsonPath("$.location").value("60 Main St"));

        assertEquals(countBeforeCreate + 1, parkingLotRepository.count());
    }

    @Test
    void databaseConstraintPreventsConcurrentDuplicateActiveLots() {
        ParkingLot firstLot = new ParkingLot();
        firstLot.setName("Constraint Garage");
        firstLot.setLocation("70 Main St");
        firstLot.setCapacity(20);
        parkingLotRepository.saveAndFlush(firstLot);

        ParkingLot duplicateLot = new ParkingLot();
        duplicateLot.setName("Constraint Garage");
        duplicateLot.setLocation("70 Main St");
        duplicateLot.setCapacity(30);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> parkingLotRepository.saveAndFlush(duplicateLot)
        );
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
