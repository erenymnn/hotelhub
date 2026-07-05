package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import java.util.List;

public interface RoomService {


    RoomResponse addRoomToHotel(RoomRequest request, String userEmail);


    RoomResponse getRoomById(Long id);


    List<RoomResponse> getRoomsByHotelId(Long hotelId);


    RoomResponse updateRoom(Long id, RoomRequest request, String userEmail);


    void deleteRoom(Long id, String userEmail);

    void setRoomAvailability(Long id, boolean isAvailable, String userEmail);
}