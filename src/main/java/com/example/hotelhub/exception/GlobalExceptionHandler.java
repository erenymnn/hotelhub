package com.example.hotelhub.exception;
import com.example.hotelhub.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

// @RestControllerAdvice: Spring'e "Bütün Controller'ları izle, hata çıkarsa bana gönder" der.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // EĞER SİSTEMDE BİR 'ResourceNotFoundException' FIRLARSA BURASI ÇALIŞACAK
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {

        // 1. Şık kuryemizi (ErrorResponse) dolduruyoruz
      ErrorResponse errorResponse=new ErrorResponse(
              LocalDateTime.now(),
              ex.getMessage(), // İçine yazdığımız "Otel Bulunamadı!" mesajını alır
              HttpStatus.NOT_FOUND.value() // 404 kodunu alır
      );
        // 2. Bunu frontend'e 404 Not Found statüsüyle dönüyoruz
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}