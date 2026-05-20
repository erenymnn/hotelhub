package com.example.hotelhub.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelSearchRequest(
        String city,
        String district,  //İlçe (Örn: Kaş)
        LocalDate checkInDate,  //giriş tarihi
        LocalDate checkOutDate, // ''
        Double minRating,  //min otel puanı
        BigDecimal maxPrice  //max gecelik fiyatı vs.
) {
}
