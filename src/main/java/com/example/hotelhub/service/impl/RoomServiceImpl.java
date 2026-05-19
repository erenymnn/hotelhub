package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.RoomMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    @Override
    public RoomResponse addRoomToHotel(RoomRequest request, String userEmail) {
        // 1. Önce ID'si verilen oteli bul
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + request.hotelId()));

        // 2. SAHİPLİK KONTROLÜ: Bu otel isteği atan kişiye mi ait?
        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Sadece kendi otelinize oda ekleyebilirsiniz!");
        }

        // 3. MapStruct ile DTO'yu Entity'ye çevir
        Room room = roomMapper.toEntity(request);

        // 4. KRİTİK DOKUNUŞ: Odayı bulduğumuz otele bağla (Zimmetle)
        room.setHotel(hotel);

        // 5. Odayı veritabanına kaydet ve dön
        Room savedRoom = roomRepository.save(room);
        return roomMapper.toResponse(savedRoom);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı!"));
        return roomMapper.toResponse(room);
    }

    @Override
    public List<RoomResponse> getRoomsByHotelId(Long hotelId) {
        // otel var mı yok mu kontrol et önce.
        if(!hotelRepository.existsById(hotelId)){
            throw new ResourceNotFoundException("Otel Bulunamadı! ID: " + hotelId);
        }
        // otele ait odaları bul ve listeye cevirip dön
        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    @Override
    public RoomResponse updateRoom(Long id, RoomRequest request, String userEmail) {
        // 1. Güncellenecek odayı bul
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı! ID: " + id));

        // 2. ZİNCİRLEME SAHİPLİK KONTROLÜ (Oda -> Otel -> Yönetici)
        if (!room.getHotel().getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu odayı güncelleme yetkiniz yok! Oda sizin otelinize ait değil.");
        }

        // 3. Eğer oda başka bir otele taşınıyorsa yeni oteli bul ve bağla
        if (!room.getHotel().getId().equals(request.hotelId())) {
            Hotel newHotel = hotelRepository.findById(request.hotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yeni Otel Bulunamadı! ID: " + request.hotelId()));

            // EĞER TAŞINIYORSA YENİ OTELİN DE KENDİSİNE AİT OLDUĞUNDAN EMİN OLMALIYIZ
            if (!newHotel.getManager().getEmail().equals(userEmail)) {
                throw new IllegalStateException("Odayı başkasının oteline taşıyamazsınız!");
            }
            room.setHotel(newHotel);
        }

        // 4. MapStruct ile diğer tüm özellikleri eski odanın üzerine yaz
        roomMapper.updateEntityFromRequest(request, room);

        // 5. Kaydet ve dön
        Room updatedRoom = roomRepository.save(room);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    public void deleteRoom(Long id, String userEmail) {
        // 1. Silinecek odayı bul
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı!"));

        // 2. ZİNCİRLEME SAHİPLİK KONTROLÜ
        if (!room.getHotel().getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu odayı silme yetkiniz yok! Oda sizin otelinize ait değil.");
        }

        // 3. Kontrolden geçtiyse sil
        roomRepository.delete(room);
    }
}