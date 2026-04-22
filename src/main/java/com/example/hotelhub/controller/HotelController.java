package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.service.HotelService;
import com.example.hotelhub.service.impl.HotelServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<HotelResponse> create(@Valid @RequestBody HotelRequest request)
    {
        return new ResponseEntity<>(hotelService.createHotel(request), HttpStatus.CREATED);
    }
}
