package tech.lokum.parkinglot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Standard error response returned for invalid or malformed API requests.
 *
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "Standard API error response")
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
