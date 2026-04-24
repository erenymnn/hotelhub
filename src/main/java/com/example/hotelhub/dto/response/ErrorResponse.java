package com.example.hotelhub.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        // JSON'a çevirirken bu formata uy diyorum
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp, // Hatanın olduğu tarih ve saat
        String message,  // "Otel bulunamadı!" gibi bizim mesajımız
        int status // hata kodu
) {
}
