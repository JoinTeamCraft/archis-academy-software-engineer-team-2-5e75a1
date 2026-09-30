package tech.lokum.parkinglot.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for expected business errors returned to API clients.
 *
 * @author Parking Lot API team
 * @version 1.0
 * @since 1.0
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
