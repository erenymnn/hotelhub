package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.HotelMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper; // Mapper'ımız görevde!

    @Override
    public HotelResponse createHotel(HotelRequest request) {
        // 1. DTO'yu Entity'ye çevir (Tek satırda MapStruct hallediyor)
        Hotel hotel = hotelMapper.toEntity(request);

        // 2. Veritabanına kaydet
        Hotel savedHotel = hotelRepository.save(hotel);

        // 3. Veritabanından dönen Entity'yi Response DTO'ya çevir ve dön
        return hotelMapper.toResponse(savedHotel);
    }

    @Override
    public List<HotelResponse> getAllHotels() {
        // Tüm otelleri çek, herbirini MapStruct'ın 'toResponse' metoduna yolla ve listele
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toResponse) // this::mapToResponse yerine Mapper'ı kullandık
                .toList();
    }

    @Override
    public HotelResponse getHotelById(Long id) {
        // Oteli bul, bulamazsan hata fırlat
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));

        // Bulunan oteli Response DTO'ya çevir
        return hotelMapper.toResponse(hotel);
    }

    @Override
    public HotelResponse updateHotel(Long id, HotelRequest request) {
        //  Güncellenecek oteli veritabanından bul yoksa hata fırlat
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));
        //  MapStruct metoduyla yeni verileri eski otelin üzerine yazdır
        hotelMapper.updateEntityFromRequest(request, hotel);
        // Güncellenmiş oteli kaydet
        Hotel updatedHotel = hotelRepository.save(hotel);
        // ve son olarak geri dön
        return hotelMapper.toResponse(updatedHotel);

    }

    @Override
    public void deleteHotel(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));
        hotelRepository.delete(hotel);
    }


}