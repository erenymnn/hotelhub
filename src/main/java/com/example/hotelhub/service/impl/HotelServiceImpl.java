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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    private final UserRepository userRepository;

    @Transactional
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
    public Page<HotelResponse> getAllHotels(Pageable pageable) {
        return hotelRepository.findAll(pageable)
                .map(hotelMapper::toResponse); // Page içindeki her Hotel'i HotelResponse'a mapler
    }

    @Override
    public Page<HotelResponse> searchHotels(HotelSearchRequest request, Pageable pageable) {
        return hotelRepository.findAll(HotelSpecification.filterHotels(request), pageable)
                .map(hotelMapper::toResponse);
    }

    @Override
    public HotelResponse getHotelById(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));


        return hotelMapper.toResponse(hotel);
    }

    @Transactional
    @Override
    public HotelResponse updateHotel(Long id, HotelRequest request, String userEmail) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));


        checkHotelOwnership(hotel, userEmail);

        hotelMapper.updateEntityFromRequest(request, hotel);


        Hotel updatedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponse(updatedHotel);
    }

    @Transactional
    @Override
    public void deleteHotel(Long id, String userEmail) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));



      checkHotelOwnership(hotel,userEmail);

        // 1. Önce otele ait tüm odaları soft delete yapıyoruz
        if (hotel.getRooms() != null) {
            hotel.getRooms().forEach(room -> room.set_Deleted(true));
        }

        hotel.set_Deleted(true);

        hotelRepository.save(hotel);
    }

    // Sınıfın en altına eklenecek yardımcı metot
    private void checkHotelOwnership(Hotel hotel, String userEmail) {
        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu işlem için yetkiniz yok! Sadece kendi otelinizi yönetebilirsiniz.");
        }
    }

}