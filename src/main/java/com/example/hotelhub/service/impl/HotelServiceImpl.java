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
    // Yöneticiyi (Manager) bulmak için UserRepository'yi enjekte ettik
    private final UserRepository userRepository;

    @Override
    public HotelResponse createHotel(HotelRequest request, String userEmail) {
        // 1. İşlemi yapan yöneticiyi veritabanından bul
        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        // 2. DTO'yu Entity'ye çevir
        Hotel hotel = hotelMapper.toEntity(request);

        // 3. GÜVENLİK/MİMARİ: Oteli bu yöneticiye ZİMMETLE
        hotel.setManager(manager); // Hotel entity'sindeki alan adına göre burayı ayarlayabilirsin

        // 4. Kaydet ve dön
        Hotel savedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponse(savedHotel);
    }

    @Override
    public List<HotelResponse> getAllHotels() {
        // Tüm otelleri çek, herbirini MapStruct'ın 'toResponse' metoduna yolla ve listele
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    @Override
    public List<HotelResponse> searchHotels(HotelSearchRequest request) {

        // 1. Yazdığımız spesifikasyonu repoya verip dinamik olarak filtreleme yapıyoruz
        return hotelRepository.findAll(HotelSpecification.filterHotels(request))
                .stream()
                .map(hotelMapper::toResponse) // Gelen otelleri Response DTO'ya çeviriyoruz
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
    public HotelResponse updateHotel(Long id, HotelRequest request, String userEmail) {
        // 1. Güncellenecek oteli veritabanından bul
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));

        // 2. VERİ SAHİPLİĞİ KONTROLÜ
        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu oteli güncelleme yetkiniz yok! Sadece kendi otelinizi güncelleyebilirsiniz.");
        }

        // 3. MapStruct metoduyla yeni verileri eski otelin üzerine yazdır
        hotelMapper.updateEntityFromRequest(request, hotel);

        // 4. Güncellenmiş oteli kaydet ve dön
        Hotel updatedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponse(updatedHotel);
    }

    @Override
    public void deleteHotel(Long id, String userEmail) {
        // 1. Silinecek oteli bul
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + id));

        // 2. VERİ SAHİPLİĞİ KONTROLÜ
        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu oteli silme yetkiniz yok! Sadece kendi otelinizi silebilirsiniz.");
        }

        // 3. İşlem geçerliyse sil
        hotelRepository.delete(hotel);
    }


}