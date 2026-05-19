package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {

    // Güvenlik (Sahiplik) kontrolü için 'String userEmail' parametresi eklendi
    RoomResponse addRoomToHotel(RoomRequest request, String userEmail);

    // Herkese açık (Okuma işlemi)
    RoomResponse getRoomById(Long id);

    // Herkese açık (Okuma işlemi)
    List<RoomResponse> getRoomsByHotelId(Long hotelId);

    // Güvenlik (Sahiplik) kontrolü için 'String userEmail' parametresi eklendi
    RoomResponse updateRoom(Long id, RoomRequest request, String userEmail);

    // Güvenlik (Sahiplik) kontrolü için 'String userEmail' parametresi eklendi
    void deleteRoom(Long id, String userEmail);
}