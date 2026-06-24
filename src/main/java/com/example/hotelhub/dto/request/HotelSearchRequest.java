package com.example.hotelhub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelSearchRequest(
        String city,
        String description,
        String district,  //İlçe (Örn: Kaş)
        LocalDate checkInDate,  //giriş tarihi
        LocalDate checkOutDate, // ''
        Double minRating,  //min otel puanı
        BigDecimal maxPrice , //max gecelik fiyatı vs.

        @Min(value = 0, message = "Sayfa numarası negatif olamaz!")
        Integer page,

        @Min(value = 1, message = "Sayfa boyutu en az 1 olmalıdır!")
        @Max(value = 100, message = "Sayfa boyutu en fazla 100 olabilir!")
        Integer size
) {
}
