package com.example.hotelhub.exception;


public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message); // Mesajı üst sınıfa gönderiyoruz
    }
}
