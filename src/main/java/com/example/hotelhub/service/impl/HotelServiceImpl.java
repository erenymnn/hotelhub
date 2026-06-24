package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.event.HotelDeleteEvent;
import com.example.hotelhub.event.HotelSyncEvent;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.HotelMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.HotelService;
import com.example.hotelhub.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final HotelMapper hotelMapper;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public HotelResponse createHotel(HotelRequest request, String userEmail) {
        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userEmail));

        Hotel hotel = hotelMapper.toEntity(request);
        hotel.setManager(manager);

        Hotel savedHotel = hotelRepository.save(hotel);

        // Senkronizasyonu tetikle
        eventPublisher.publishEvent(createHotelSyncEvent(savedHotel));

        return hotelMapper.toResponse(savedHotel);
    }

    @Override
    public Page<HotelResponse> getAllHotels(Pageable pageable) {
        return hotelRepository.findAll(pageable).map(hotelMapper::toResponse);
    }

    @Override
    public Page<HotelResponse> searchHotels(HotelSearchRequest request, Pageable pageable) {
        log.info("Arama başlatıldı: {}", request.city());
        return hotelRepository.findAll(HotelSpecification.filterHotels(request), pageable)
                .map(hotelMapper::toResponse);
    }

    @Override
    public HotelResponse getHotelById(Long id) {
        return hotelMapper.toResponse(findHotelById(id));
    }

    @Transactional
    @Override
    public HotelResponse updateHotel(Long id, HotelRequest request, String userEmail) {
        Hotel hotel = findHotelById(id);
        assertHotelOwnership(hotel, userEmail);

        hotelMapper.updateEntityFromRequest(request, hotel);
        Hotel updatedHotel = hotelRepository.save(hotel);

        // Güncelleme sonrası Elasticsearch senkronizasyonunu tetikle
        eventPublisher.publishEvent(createHotelSyncEvent(updatedHotel));

        return hotelMapper.toResponse(updatedHotel);
    }

    @Transactional
    @Override
    public void deleteHotel(Long id, String userEmail) {
        Hotel hotel = findHotelById(id);
        assertHotelOwnership(hotel, userEmail);

        roomRepository.softDeleteByHotelId(id);
        hotel.setDeleted(true);
        hotelRepository.save(hotel);

        // ELASTICSEARCH'TE SİLİNMESİ İÇİN EVENT TETİKLE
        eventPublisher.publishEvent(new HotelDeleteEvent(id));
    }

    @Override
    public Page<HotelResponse> getMyHotels(String userEmail, Pageable pageable) {
        return hotelRepository.findAll((root, query, cb) ->
                        cb.equal(root.get("manager").get("email"), userEmail), pageable)
                .map(hotelMapper::toResponse);
    }

    // Yardımcı Metotlar
    private Hotel findHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel bulunamadı! ID: " + id));
    }

    private void assertHotelOwnership(Hotel hotel, String userEmail) {
        if (hotel.getManager() == null || !Objects.equals(hotel.getManager().getEmail(), userEmail)) {
            throw new AccessDeniedException("Bu otel üzerinde işlem yapma yetkiniz yok!");
        }
    }

    private HotelSyncEvent createHotelSyncEvent(Hotel savedHotel) {
        return new HotelSyncEvent(
                savedHotel.getId(),
                savedHotel.getName(),
                savedHotel.getDescription(),
                savedHotel.getCity(),
                savedHotel.getDistrict(),
                savedHotel.getRating()
        );
    }

}