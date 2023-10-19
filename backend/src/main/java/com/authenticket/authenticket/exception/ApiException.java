package com.authenticket.authenticket.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

/**
 * A simple data class representing an API exception message.
 */
@Data
@AllArgsConstructor
public class ApiException {
    private final String message;
//    private final HttpStatus httpStatus;
//    private final ZonedDateTime timestamp;

//    public ApiException(String message, HttpStatus httpStatus, ZonedDateTime timestamp) {
//        this.message = message;
//        this.httpStatus = httpStatus;
//        this.timestamp = timestamp;
//    }

//    public String getMessage() {
//        return message;
//    }
//
//    public HttpStatus getHttpStatus() {
//        return httpStatus;
//    }
//
//    public ZonedDateTime getTimestamp() {
//        return timestamp;
//    }
}
