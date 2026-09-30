package tech.lokum.parkinglot.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
