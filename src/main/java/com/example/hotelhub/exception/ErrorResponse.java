package com.example.hotelhub.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp, // Hatanın ne zaman olduğu
        int status,              // HTTP Statü Kodu (400, 404, 500)
        String error,            // Hatanın kısa türü (Not Found, Bad Request)
        String message,          // Bizim fırlattığımız özel mesaj ("Oda bulunamadı!")
        String path
) {
}
