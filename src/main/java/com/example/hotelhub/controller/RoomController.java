package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody RoomRequest request)
    {
        return new ResponseEntity<>(roomService.addRoomToHotel(request), HttpStatus.CREATED);
    }
}
