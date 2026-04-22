package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.repo.HotelRepository;
import com.example.hotelhub.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Override
    public HotelResponse createHotel(HotelRequest request) {
        // 1. DTO'yu Entity'ye çevir (Map)
        Hotel hotel=new Hotel();
        hotel.setName(request.name());
        hotel.setCity(request.city());
        hotel.setAddress(request.address());
        hotel.setRating(request.rating());

// 2. Veritabanına kaydet
        Hotel savedHotel=hotelRepository.save(hotel);

return  new HotelResponse(
        savedHotel.getId(),
        savedHotel.getName(),
        savedHotel.getCity(),
        savedHotel.getAddress(),
        savedHotel.getRating()
);


    }
}
