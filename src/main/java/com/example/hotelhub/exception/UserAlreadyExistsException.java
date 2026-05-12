package com.example.hotelhub.exception;

public class UserAlreadyExistsException extends  RuntimeException{
    public UserAlreadyExistsException(String message) {
        super(message); // Mesajı üst sınıfa gönderiyoruz
    }
}
