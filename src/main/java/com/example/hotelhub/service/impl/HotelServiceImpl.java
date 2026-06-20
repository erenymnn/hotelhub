package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.HotelMapper;
import com.example.hotelhub.messaging.producer.BookingProducer; // RabbitMQ Producer Import Edildi

import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.HotelService;
import com.example.hotelhub.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // Lombok bizim yerimize bookingProducer'ı otomatik enjekte edecek
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final UserRepository userRepository;
    private final BookingProducer bookingProducer; // RabbitMQ bağımlılığı eklendi

    // Kurumsal log nesnemiz
    private static final Logger log = LoggerFactory.getLogger(HotelServiceImpl.class);

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
                .map(hotelMapper::toResponse);
    }

    @Override
    public Page<HotelResponse> searchHotels(HotelSearchRequest request, Pageable pageable) {
        // 1. Kurumsal Loglama
        log.info("Hotel search triggered for city: {} with page: {}", request.city(), pageable.getPageNumber());

        try {
            // 2. ASENKRON SİHİR: Arama anında kuyruğa mesajı fırlatıyoruz
            bookingProducer.sendBookingNotification("User searched for city: " + request.city() + " on page: " + pageable.getPageNumber());

            // 3. Veritabanı Sorgusu
            return hotelRepository.findAll(HotelSpecification.filterHotels(request), pageable)
                    .map(hotelMapper::toResponse);
        } catch (Exception e) {
            // Bir hata oluşursa ERROR seviyesinde logluyoruz
            log.error("Error occurred while searching hotels for city: {}", request.city(), e);
            throw e;
        }
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

        checkHotelOwnership(hotel, userEmail);

        if (hotel.getRooms() != null) {
            hotel.getRooms().forEach(room -> room.set_Deleted(true));
        }

        hotel.set_Deleted(true);
        hotelRepository.save(hotel);
    }

    private void checkHotelOwnership(Hotel hotel, String userEmail) {
        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu işlem için yetkiniz yok! Sadece kendi otelinizi yönetebilirsiniz.");
        }
    }
}