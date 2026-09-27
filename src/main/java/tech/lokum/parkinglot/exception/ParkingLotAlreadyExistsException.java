package tech.lokum.parkinglot.exception;

import org.springframework.http.HttpStatus;

/**
 * Indicates an active parking lot already exists with the requested name and location.
 *
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
public class ParkingLotAlreadyExistsException extends BusinessException {

    public ParkingLotAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "An active parking lot already exists with this name and location");
    }
}
