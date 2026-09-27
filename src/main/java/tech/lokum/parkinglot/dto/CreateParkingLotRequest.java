package tech.lokum.parkinglot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Validated request body for {@code POST /api/parking-lots}.
 *
 * @param name required, non-blank parking lot name
 * @param location required, non-blank parking lot location
 * @param capacity required positive maximum vehicle capacity
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Details required to create a parking lot")
public record CreateParkingLotRequest(
        @Schema(description = "Parking lot name", example = "Central Garage")
        @NotBlank String name,
        @Schema(description = "Street address or other location description", example = "123 Main St")
        @NotBlank String location,
        @Schema(description = "Maximum number of vehicles", example = "120", minimum = "1")
        @Positive int capacity
) {
}
