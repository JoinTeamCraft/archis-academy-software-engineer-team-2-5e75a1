package tech.lokum.parkinglot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Validated request body for {@code POST /api/parking-lots}.
 *
 * @param name required, non-blank parking lot name
 * @param address required, non-blank parking lot location
 * @param capacity required positive maximum vehicle capacity
 * @param operatorId required ID of the existing OPERATOR user who owns the parking lot;
 *                   administrators creating a lot for an operator must supply that operator's ID
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Details required to create a parking lot")
public record CreateParkingLotRequest(
        @Schema(description = "ID of the owning operator", example = "42", minimum = "1")
        @NotNull @Positive Long operatorId,
        @Schema(description = "Parking lot name", example = "Central Garage")
        @NotBlank String name,
        @Schema(description = "Street address or other address description", example = "123 Main St")
        @NotBlank String address,
        @Schema(description = "Maximum number of vehicles", example = "120", minimum = "1")
        @Positive int capacity
) {
}
