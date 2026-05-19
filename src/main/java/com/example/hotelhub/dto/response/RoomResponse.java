package com.example.hotelhub.dto.response;

import com.example.hotelhub.entity.RoomType;

import java.math.BigDecimal;
import java.util.List;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomType type,
        String viewType, // Manzara dışarıya açıldı
        BigDecimal pricePerNight,
        Integer capacity,
        Boolean hasAirConditioning, // Klima dışarıya açıldı
        Boolean hasBalcony, // Balkon dışarıya açıldı
        List<String> features,
        Boolean isAvailable
) {
}
