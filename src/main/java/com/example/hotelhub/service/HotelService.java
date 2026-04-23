package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;

import java.util.List;

public interface HotelService {
    HotelResponse createHotel(HotelRequest request);

    List<HotelResponse> getAllHotels();
    HotelResponse getHotelById(Long id);

}
