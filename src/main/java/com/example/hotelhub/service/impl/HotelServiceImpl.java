package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.dto.response.PageResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.Role;
import com.example.hotelhub.event.HotelDeleteEvent;
import com.example.hotelhub.event.HotelSyncEvent;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.HotelMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.HotelService;
import com.example.hotelhub.service.RedisCacheService;
import com.example.hotelhub.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final RedisCacheService redisCacheService;
    private final CacheManager cacheManager;

    @Autowired
    @Lazy
    private HotelServiceImpl self;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    @CacheEvict(value = {"hotelsCache", "topRatedHotels"}, allEntries = true)
    public HotelResponse createHotel(HotelRequest request, String userEmail) {
        User manager = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userEmail));

        Hotel hotel = hotelMapper.toEntity(request);
        hotel.setManager(manager);

        Hotel savedHotel = hotelRepository.save(hotel);
        HotelResponse response = hotelMapper.toResponse(savedHotel);
        // Senkronizasyonu tetikle yani otel güncellendiginde elasticsearch de haber veriyorsun.
        eventPublisher.publishEvent(createHotelSyncEvent(savedHotel));

        // Oteli veritabanına kaydeder kaydetmez, doğrudan Redis'e de (rastgele TTL ile) yazıyoruz. write-through
        redisCacheService.saveWithJitter("hotelDetails::" + savedHotel.getId(), response);
        return response;
    }

    @Override
    @Cacheable(value = "hotelsCache")
    public PageResponse<HotelResponse> getAllHotels(Pageable pageable) {

        Page<Hotel> hotelPage = hotelRepository.findAll(pageable);
        List<HotelResponse> content = hotelPage.getContent().stream()
                .map(hotelMapper::toResponse)
                .toList();


        return PageResponse.of(hotelPage, content);
    }

    @Override
    public PageResponse<HotelResponse> searchHotels(HotelSearchRequest request, Pageable pageable) {
        log.info("Arama başlatıldı: {}", request.city());

        Page<Hotel> hotelPage = hotelRepository.findAll(HotelSpecification.filterHotels(request), pageable);
        List<HotelResponse> content = hotelPage.getContent().stream()
                .map(hotelMapper::toResponse)
                .toList();


        return PageResponse.of(hotelPage, content);
    }

    @Override
    public HotelResponse getHotelById(Long id) {
        // Veriyi doğrudan DB'den değil, kendi Proxy'miz (self) üzerinden Cache'den istiyoruz
        HotelResponse response = self.fetchHotelOrNull(id);
        if (response == null) {
            throw new ResourceNotFoundException("Otel bulunamadı! ID: " + id);
        }
        return response;
    }
    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = {"hotelsCache", "topRatedHotels"}, allEntries = true),
            @CacheEvict(value = "hotelDetails", key = "#id")
    })
    public HotelResponse updateHotel(Long id, HotelRequest request, String userEmail) {
        Hotel hotel = findHotelById(id);
        assertHotelOwnership(hotel, userEmail);

        hotelMapper.updateEntityFromRequest(request, hotel);
        Hotel updatedHotel = hotelRepository.save(hotel);
        HotelResponse response = hotelMapper.toResponse(updatedHotel);

        eventPublisher.publishEvent(createHotelSyncEvent(updatedHotel));


        log.info("Otel güncellendi ve Cache'i temizlendi! ID: {}", id);
        return response;
    }

    @Transactional
    @Override
    @Caching(evict = {
            // Genel listeyi ve Anasayfa vitrinini tamamen temizle
            @CacheEvict(value = {"hotelsCache", "topRatedHotels"}, allEntries = true),
            // SİLİNEN otelin detay sayfasını Redis'ten uçur
            @CacheEvict(value = "hotelDetails", key = "#id"),
            //  Silinen otelin liste halindeki odalarını uçur
            @CacheEvict(value = "roomsByHotel", key = "#id")
    })
    public void deleteHotel(Long id, String userEmail) {
        Hotel hotel = findHotelById(id);
        assertHotelOwnership(hotel, userEmail);

        // Önce bu otele ait odaları veritabanından çek (Silinmeden önce ID'lerini almalıyız!)
        List<Room> rooms = roomRepository.findByHotelId(id);

        // Veritabanı Silme İşlemleri
        roomRepository.softDeleteByHotelId(id); // önce odaları sil
        hotel.setDeleted(true); // oteli sil
        hotelRepository.save(hotel);

        // MANUEL CACHE TEMİZLİĞİ
       Cache roomDetailsCache = cacheManager.getCache("roomDetails");
        if (roomDetailsCache != null && rooms != null) {
            for (Room room : rooms) {
                roomDetailsCache.evict(room.getId());
            }
        }

        eventPublisher.publishEvent(new HotelDeleteEvent(id));
    }

    @Override
    public PageResponse<HotelResponse> getMyHotels(String userEmail, Pageable pageable) {

        Page<Hotel> hotelPage = hotelRepository.findAll((root, query, cb) ->
                cb.equal(root.get("manager").get("email"), userEmail), pageable);

        List<HotelResponse> content = hotelPage.getContent().stream()
                .map(hotelMapper::toResponse)
                .toList();

        return PageResponse.of(hotelPage, content);
    }

    // Yardımcı Metotlar
    private Hotel findHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otel bulunamadı! ID: " + id));
    }

    private void assertHotelOwnership(Hotel hotel, String userEmail) {
        // Veritabanına İNMEDEN, hafızadaki JWT'den gelen kimlik bilgilerini alıyoruz
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //  JWT'nin içine koyduğumuz roller arasında ADMIN var mı diye bakıyoruz
        boolean isAdmin = (auth != null) && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        // ADMIN değilse ve otelin sahibi de o değilse hata fırlat
        if (!isAdmin && (hotel.getManager() == null || !Objects.equals(hotel.getManager().getEmail(), userEmail))) {
            throw new AccessDeniedException("Bu işlem için yetkiniz yok!");
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
    // Sadece Cache ve DB ile konuşur. Bulamazsa Exception atmaz, NULL döner.
    @Cacheable(value = "hotelDetails", key = "#id", sync = true)
    public HotelResponse fetchHotelOrNull(Long id) {
        return hotelRepository.findById(id)
                .map(hotelMapper::toResponse)
                .orElse(null); // Yoksa null dön (Redis bunu null olarak önbellekleyecek)
    }

}