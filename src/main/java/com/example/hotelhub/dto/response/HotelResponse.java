package com.example.hotelhub.dto.response;

import java.util.List;

public record HotelResponse(

        Long id,
        String name,
        String city,
        String district,
        String address,
        String number,
        String email,
        String description,
        Double rating ,
        List<RoomResponse> rooms
) {
}
