package com.example.hotelhub.dto.response;

import com.example.hotelhub.entity.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long roomId,
        Long userId, // Hangi kullanıcının rezervasyonu olduğunu görebilmek için
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalPrice, // Hesaplanmış toplam tutar
        BookingStatus status
) {
}
