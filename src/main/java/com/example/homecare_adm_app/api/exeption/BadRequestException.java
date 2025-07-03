package com.example.homecare_adm_app.api.exeption;


public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}