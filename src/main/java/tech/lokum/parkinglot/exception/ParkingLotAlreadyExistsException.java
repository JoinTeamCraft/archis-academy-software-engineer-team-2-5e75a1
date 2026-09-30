package tech.lokum.parkinglot.exception;

import org.springframework.http.HttpStatus;

/**
 * Indicates a parking lot already exists with the requested name and location.
 *
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
public class ParkingLotAlreadyExistsException extends BusinessException {

    public ParkingLotAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "A parking lot already exists with this name and location");
    }
}
