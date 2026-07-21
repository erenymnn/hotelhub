package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.Role;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.RoomMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.RedisCacheService;
import com.example.hotelhub.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RedisCacheService redisCacheService;
    private final CacheManager cacheManager;
    private final RoomMapper roomMapper;
    @Autowired
    @Lazy
    private RoomServiceImpl self;

    @Transactional
    @Override
    @CacheEvict(value = "roomsByHotel", key = "#request.hotelId()")
    public RoomResponse addRoomToHotel(RoomRequest request, String userEmail) {
        Hotel hotel = findHotelById(request.hotelId());
        assertHotelOwnership(hotel, userEmail);

        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);
        room.setIsAvailable(true); // Varsayılan değer
        Room savedRoom = roomRepository.save(room);
        RoomResponse response = roomMapper.toResponse(savedRoom);
// Odayı DB'ye yazar yazmaz, Jitter (rastgele süre) ile direkt Redis'e de atıyoruz!
        redisCacheService.saveWithJitter("roomDetails::" + savedRoom.getId(), response);
        return response;
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        // Veriyi doğrudan DB'den veya local metottan değil, Proxy (self) üzerinden istiyoruz.
        RoomResponse response = self.fetchRoomOrNull(id);

        if (response == null) {
            throw new ResourceNotFoundException("Oda bulunamadı! ID: " + id);
        }
        return response;
    }

    @Override
    public List<RoomResponse> getRoomsByHotelId(Long hotelId) {
        // Veriyi Cache üzerinden (Proxy yardımıyla) istiyoruz
        List<RoomResponse> response = self.fetchRoomsByHotelOrNull(hotelId);

        if (response == null) {
            throw new ResourceNotFoundException("Otel bulunamadı! ID: " + hotelId);
        }
        return response;
    }
    @Transactional
    @Override
    // Sadece odanın tekil detayını anotasyonla siliyoruz.
    @CacheEvict(value = "roomDetails", key = "#id")
    public RoomResponse updateRoom(Long id, RoomRequest request, String userEmail) {
        Room room = findRoomById(id);
        assertHotelOwnership(room.getHotel(), userEmail);

        Long oldHotelId = room.getHotel().getId();
        Long newHotelId = request.hotelId();

        // Eğer oda başka bir otele taşınıyorsa, yeni otelin de sahibi olduğunu doğrula
        if (!Objects.equals(oldHotelId, newHotelId)) {
            Hotel newHotel = findHotelById(newHotelId);
            assertHotelOwnership(newHotel, userEmail);
            room.setHotel(newHotel);
        }

        roomMapper.updateEntityFromRequest(request, room);
        Room updatedRoom = roomRepository.save(room);

        // --- MANUEL CACHE TEMİZLİĞİ ---
        // Her halükarda hedeflenen yeni/güncel otelin listesini çöpe at
        Objects.requireNonNull(cacheManager.getCache("roomsByHotel")).evict(newHotelId);

        // Eğer otel değiştiyse, eski otelin listesini de çöpe at ki "hayalet oda" kalmasın!
        if (!Objects.equals(oldHotelId, newHotelId)) {
            Objects.requireNonNull(cacheManager.getCache("roomsByHotel")).evict(oldHotelId);
        }

        return roomMapper.toResponse(updatedRoom);
    }
    @Transactional
    @Override
    public void deleteRoom(Long id, String userEmail) {
        Room room = findRoomById(id);
        assertHotelOwnership(room.getHotel(), userEmail);
// Silmeden önce bağlı olduğu otelin ID'sini alıyoruz ki Cache'i doğru silebilelim
        Long hotelId = room.getHotel().getId();
        room.setDeleted(true);
        roomRepository.save(room);
        // Bağlı olduğu otelin oda listesini programatik olarak RAM'den uçur!
        Objects.requireNonNull(cacheManager.getCache("roomsByHotel")).evict(hotelId);
    }

    @Transactional
    @Override
    @CacheEvict(value = "roomDetails", key = "#id")
    public void setRoomAvailability(Long id, boolean isAvailable, String userEmail) {
        Room room = findRoomById(id);
        assertHotelOwnership(room.getHotel(), userEmail);
        Long hotelId = room.getHotel().getId();
        room.setIsAvailable(isAvailable);
        roomRepository.save(room);
        // Birisi odayı rezerve ettiğinde (isAvailable = false olduğunda),
        // o otelin oda listesini anında çöpe atıyoruz. Böylece "Overbooking" riskini SIFIRA indiriyoruz!
        Objects.requireNonNull(cacheManager.getCache("roomsByHotel")).evict(hotelId);
    }


    private Hotel findHotelById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Otel bulunamadı! ID: " + hotelId));
    }

    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı! ID: " + roomId));
    }

    private void assertHotelOwnership(Hotel hotel, String userEmail) {

        //  Veritabanına (userRepository'e) HİÇ İNMEDEN hafızadaki JWT yetkilerini alıyoruz
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //  JWT'den gelen roller arasında ADMIN var mı kontrol ediyoruz
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        //  Eğer ADMIN değilse VE işlemi yapan kişi otelin yöneticisi/sahibi değilse hata fırlat
        if (!isAdmin && (hotel.getManager() == null || !Objects.equals(hotel.getManager().getEmail(), userEmail))) {
            throw new AccessDeniedException("Bu işlem için yetkiniz yok!");
        }
    }
    // Hata fırlatan sonuçları Redis'e almamak ve Penetration saldırılarını önlemek için NULL döneriz.
    @Cacheable(value = "roomDetails", key = "#id", sync = true)
    public RoomResponse fetchRoomOrNull(Long id) {
        return roomRepository.findById(id)
                .map(roomMapper::toResponse)
                .orElse(null); // Yoksa Exception atmaz, null döner (Redis bunu null olarak önbellekler)
    }
    // Exception fırlatmayan, sadece veriyi (veya yokluğu) dönen asıl Cache metodumuz
    @Cacheable(value = "roomsByHotel", key = "#hotelId", sync = true)
    public List<RoomResponse> fetchRoomsByHotelOrNull(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            // Otel yoksa Exception fırlatma! Geriye NULL dön ki Redis "Bu otel yok" bilgisini cache'lesin.
            return null;
        }
        //  Otel varsa odaları getir. (Hiç oda yoksa boş liste döner, Spring boş listeyi de başarıyla cache'ler)
        return roomRepository.findAvailableRoomsByHotelId(hotelId)
                .stream()
                .map(roomMapper::toResponse)
                .toList();
    }
}