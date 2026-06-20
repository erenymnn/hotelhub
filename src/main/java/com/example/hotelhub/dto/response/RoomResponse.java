package com.example.hotelhub.dto.response;

import com.example.hotelhub.entity.enums.RoomType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomType type,
        String viewType,
        BigDecimal pricePerNight,
        Integer capacity,
        Boolean hasAirConditioning,
        Boolean hasBalcony,
        List<String> features,
        Boolean isAvailable
) {
}
