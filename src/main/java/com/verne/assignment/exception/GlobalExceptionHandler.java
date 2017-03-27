package com.verne.assignment.exception;

import com.verne.assignment.model.Error;
import jdk.nashorn.internal.objects.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler that returns correct status code when an exception occurs
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger logger = LoggerFactory
            .getLogger(GlobalExceptionHandler.class);

    /**
     * Handler for when an IllegalArgumentException occurs
     * @param e
     * @param request
     * @return Bad request ResponseEntity
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Error> illegalArgumentException(RuntimeException e, WebRequest request) {
        logger.error("Error: ",e);
        return new ResponseEntity(new Error(e.getMessage()), null, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handler for when a GeoLocationServiceException occurs
     * @param e
     * @param request
     * @return Internal server error ResponseEntity
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Error> serviceException(Exception e, WebRequest request) {
        logger.error("Error: ",e);
        return new ResponseEntity(new Error(e.getMessage()), null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}