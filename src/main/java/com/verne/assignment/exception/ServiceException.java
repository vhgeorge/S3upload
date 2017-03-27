package com.verne.assignment.exception;

public class ServiceException extends RuntimeException {
    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message){
        super(message);
    }
}
