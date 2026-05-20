package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.HotelMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.HotelService;
import com.example.hotelhub.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    private final UserRepository userRepository;

    @Override
    public HotelResponse createHotel(HotelRequest request, String userEmail) {

        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));


        Hotel hotel = hotelMapper.toEntity(request);


        hotel.setManager(manager);


        Hotel savedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponse(savedHotel);
    }

    @Override
    public List<HotelResponse> getAllHotels() {

        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    @Override
    public List<HotelResponse> searchHotels(HotelSearchRequest request) {


        return hotelRepository.findAll(HotelSpecification.filterHotels(request))
                .stream()
                .map(hotelMapper::toResponse) // Gelen otelleri Response DTO'ya çeviriyoruz
                .toList();
    }

    @Override
    public HotelResponse getHotelById(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));


        return hotelMapper.toResponse(hotel);
    }

    @Override
    public HotelResponse updateHotel(Long id, HotelRequest request, String userEmail) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));


        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu oteli güncelleme yetkiniz yok! Sadece kendi otelinizi güncelleyebilirsiniz.");
        }


        hotelMapper.updateEntityFromRequest(request, hotel);


        Hotel updatedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponse(updatedHotel);
    }

    @Override
    public void deleteHotel(Long id, String userEmail) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));


        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu oteli silme yetkiniz yok! Sadece kendi otelinizi silebilirsiniz.");
        }


        hotelRepository.delete(hotel);
    }


}