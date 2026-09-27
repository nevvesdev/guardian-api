package com.nevvesdev.guardianapi.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, String identifier) {
        super(HttpStatus.NOT_FOUND, resource + " não encontrado: " + identifier);
    }
}