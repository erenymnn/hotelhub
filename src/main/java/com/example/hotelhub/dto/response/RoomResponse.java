package com.example.hotelhub.dto.response;

import com.example.hotelhub.entity.RoomType;

import java.math.BigDecimal;
import java.util.List;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomType type,
        BigDecimal pricePerNight,
        Integer capacity,
        List<String> features,
        Boolean isAvailable
) {
}
