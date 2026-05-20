package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;

import java.util.List;

public interface HotelService {
    // Güvenlik ve sahiplik kontrolü için 'String userEmail' parametreleri eklendi
    HotelResponse createHotel(HotelRequest request, String userEmail);

    List<HotelResponse> getAllHotels();

    HotelResponse getHotelById(Long id);

    HotelResponse updateHotel(Long id, HotelRequest request, String userEmail);

    void deleteHotel(Long id, String userEmail);

    List<HotelResponse> searchHotels(HotelSearchRequest request);

}
