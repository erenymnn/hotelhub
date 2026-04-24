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


@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    @Override
    public RoomResponse addRoomToHotel(RoomRequest request) {
        // 1. Önce ID'si verilen oteli bul
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + request.hotelId()));
// 2. MapStruct ile DTO'yu Entity'ye çevir (İçinde henüz otel yok)
        Room room = roomMapper.toEntity(request);
// 3. KRİTİK DOKUNUŞ: Odayı bulduğumuz otele bağla (Zimmetle)
        room.setHotel(hotel);
// 4. Odayı veritabanına kaydet
        Room savedRoom = roomRepository.save(room);
// 5. Kaydedilen odayı tekrar DTO'ya çevirip dışarı dön
        return roomMapper.toResponse(savedRoom);


    }
}
