package com.upc.oss.monitoreo.exception;

public class CompanyNotFound extends RuntimeException{
    public CompanyNotFound(String message) {
        super(message);
    }
}
