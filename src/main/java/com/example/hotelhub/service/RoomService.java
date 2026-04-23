package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;


public interface RoomService {
    RoomResponse addRoomToHotel(RoomRequest request);
}
