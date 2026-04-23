package com.example.hotelhub.dto.response;

import com.example.hotelhub.entity.RoomType;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomType type,
        BigDecimal pricePerNight,
        Integer capacity,
        Boolean isAvailable
) {
}
