package com.upc.oss.monitoreo.exception;

public class CompanyAlreadyExists extends RuntimeException{
    public CompanyAlreadyExists(String message) {
        super(message);
    }
}
