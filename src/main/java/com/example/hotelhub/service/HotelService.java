package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {

    HotelResponse createHotel(HotelRequest request, String userEmail);

    Page<HotelResponse> getAllHotels(Pageable pageable);
    Page<HotelResponse> searchHotels(HotelSearchRequest request, Pageable pageable);

    HotelResponse getHotelById(Long id);

    HotelResponse updateHotel(Long id, HotelRequest request, String userEmail);

    void deleteHotel(Long id, String userEmail);

    Page<HotelResponse> getMyHotels(String userEmail, Pageable pageable);

}
