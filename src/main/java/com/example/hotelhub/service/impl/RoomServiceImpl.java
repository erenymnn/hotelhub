package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.repo.HotelRepository;
import com.example.hotelhub.repo.RoomRepository;
import com.example.hotelhub.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Override
    public RoomResponse addRoomToHotel(RoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new RuntimeException("Otel Bulunamadı! ID: " + request.hotelId()));
// 2. DTO'dan gelen verilerle yeni bir Oda (Entity) oluştur
        Room room = new Room();
        room.setRoomNumber(request.roomNumber());
        room.setType(request.type());
        room.setPricePerNight(request.pricePerNight());
        room.setCapacity(request.capacity());
// ÖNEMLİ: Odaya bulduğumuz oteli atıyoruz. Bu sayede veritabanında "hotel_id" sütunu dolacak.
        room.setHotel(hotel);
// 3. Odayı veritabanına kaydet
        Room savedRoom = roomRepository.save(room);
// 4. Temiz kod (Clean Code) prensibiyle manuel yazdığımız yardımcı metoda gönderip Response dön
        return mapToResponse(savedRoom);


    }

    // DRY (Don't Repeat Yourself) Prensibi için yardımcı metot
    private RoomResponse mapToResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getType(),
                room.getPricePerNight(),
                room.getCapacity(),
                room.getIsAvailable()
        );
    }
}
