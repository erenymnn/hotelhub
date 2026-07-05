package com.example.hotelhub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelSearchRequest(
        String city,
        String description,
        String district, //İlçe (Örn: Kaş)
        LocalDate checkInDate, //giriş tarihi
        LocalDate checkOutDate, //
        @PositiveOrZero(message = "Puan negatif olamaz!")
        Double minRating,   //min otel puanı
        @PositiveOrZero(message = "Fiyat negatif olamaz!")
        BigDecimal maxPrice , //max gecelik fiyatı vs.

        @Min(value = 0, message = "Sayfa numarası negatif olamaz!")
        Integer page,

        @Min(value = 1, message = "Sayfa boyutu en az 1 olmalıdır!")
        @Max(value = 100, message = "Sayfa boyutu en fazla 100 olabilir!") //kullanıcı 1000 girebilir sistem çökmemesi için
        Integer size
) {
}
