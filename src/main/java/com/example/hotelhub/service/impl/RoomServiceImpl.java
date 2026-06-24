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
import org.springframework.security.access.AccessDeniedException;
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
    private final RoomMapper roomMapper;

    @Transactional
    @Override
    public RoomResponse addRoomToHotel(RoomRequest request, String userEmail) {
        Hotel hotel = findHotelById(request.hotelId());
        assertHotelOwnership(hotel, userEmail);

        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);
        room.setIsAvailable(true); // Varsayılan değer

        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        return roomMapper.toResponse(findRoomById(id));
    }

    @Override
    public List<RoomResponse> getRoomsByHotelId(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Otel bulunamadı! ID: " + hotelId);
        }
        return roomRepository.findAvailableRoomsByHotelId(hotelId)
                .stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public RoomResponse updateRoom(Long id, RoomRequest request, String userEmail) {
        Room room = findRoomById(id);
        assertHotelOwnership(room.getHotel(), userEmail);

        // Eğer oda başka bir otele taşınıyorsa, yeni otelin de sahibi olduğunu doğrula
        if (!Objects.equals(room.getHotel().getId(), request.hotelId())) {
            Hotel newHotel = findHotelById(request.hotelId());
            assertHotelOwnership(newHotel, userEmail);
            room.setHotel(newHotel);
        }

        roomMapper.updateEntityFromRequest(request, room);
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional
    @Override
    public void deleteRoom(Long id, String userEmail) {
        Room room = findRoomById(id);
        assertHotelOwnership(room.getHotel(), userEmail);

        room.setDeleted(true);
        roomRepository.save(room);
    }

    @Transactional
    @Override
    public void setRoomAvailability(Long id, boolean isAvailable, String userEmail) {
        Room room = findRoomById(id);
        assertHotelOwnership(room.getHotel(), userEmail);

        room.setIsAvailable(isAvailable);
        roomRepository.save(room);
    }

    //  Yardımcı Metotlar
    private Hotel findHotelById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Otel bulunamadı! ID: " + hotelId));
    }

    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı! ID: " + roomId));
    }

    private void assertHotelOwnership(Hotel hotel, String userEmail) {
        if (hotel.getManager() == null || !Objects.equals(hotel.getManager().getEmail(), userEmail)) {
            throw new AccessDeniedException("Bu işlem için yetkiniz yok!");
        }
    }
}