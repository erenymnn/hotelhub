package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HotelService {

    HotelResponse createHotel(HotelRequest request, String userEmail);

    PageResponse<HotelResponse> getAllHotels(Pageable pageable);
    PageResponse<HotelResponse> searchHotels(HotelSearchRequest request, Pageable pageable);

    HotelResponse getHotelById(Long id);

    HotelResponse updateHotel(Long id, HotelRequest request, String userEmail);

    void deleteHotel(Long id, String userEmail);

    PageResponse<HotelResponse> getMyHotels(String userEmail, Pageable pageable);

}
