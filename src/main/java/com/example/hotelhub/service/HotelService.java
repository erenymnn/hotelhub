package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;

public interface HotelService {
    HotelResponse createHotel(HotelRequest request);
}
