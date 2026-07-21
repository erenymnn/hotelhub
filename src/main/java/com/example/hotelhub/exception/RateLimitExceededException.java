package com.example.hotelhub.exception;

/**
 * Rate limit aşıldığında fırlatılan exception.
 *
 * Filter seviyesinde direkt JSON yanıtı yazılsa da,
 * eğer istek bir şekilde controller'a ulaşırsa
 * GlobalExceptionHandler bu exception'ı yakalayıp 429 döner.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
