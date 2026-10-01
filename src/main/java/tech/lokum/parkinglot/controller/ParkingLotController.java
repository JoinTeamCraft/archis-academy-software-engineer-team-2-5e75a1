package tech.lokum.parkinglot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tech.lokum.parkinglot.dto.CreateParkingLotRequest;
import tech.lokum.parkinglot.dto.ErrorResponse;
import tech.lokum.parkinglot.dto.ParkingLotResponse;
import tech.lokum.parkinglot.service.ParkingLotService;

/**
 * REST endpoints for creating and managing parking lots.
 *
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/parking-lots")
@Tag(name = "Parking Lots", description = "Parking lot creation and management")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    /**
     * Creates a parking lot.
     *
     * <p>Accepts a non-blank name and address and a positive capacity. Returns the persisted
     * parking lot with its generated identifier.</p>
     *
     * @param request validated parking lot creation details
     * @return the created parking lot
     * @since 1.0
     */
    @Operation(
            summary = "Create a parking lot",
            description = """
                Creates a parking lot for an existing OPERATOR user identified by operatorId.
                An administrator creating a lot on an operator's behalf supplies the owning
                operator's ID. The name and address must be unique among active parking lots.
                """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Parking lot created",
            content = @Content(schema = @Schema(implementation = ParkingLotResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body is malformed or contains invalid fields",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "An active parking lot already exists with this name and address",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "The requested user is not an operator",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "The requested operator does not exist",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingLotResponse createParkingLot(@Valid @RequestBody CreateParkingLotRequest request) {
        return parkingLotService.createParkingLot(request);
    }
}
