package tech.lokum.parkinglot.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Public representation of a created parking lot.
 *
 * @param id generated parking lot identifier
 * @param name parking lot name
 * @param address parking lot location
 * @param capacity maximum number of vehicles
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Created parking lot")
public record ParkingLotResponse(
        @Schema(description = "Generated parking lot identifier", example = "1")
        Long id,
        @Schema(description = "Parking lot name", example = "Central Garage")
        String name,
        @Schema(description = "Street address ", example = "123 Main St")
        String address,
        @Schema(description = "Maximum number of vehicles", example = "120")
        int capacity
) {
}
