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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Otel Bulunamadı! ID: " + request.hotelId()));


        if (!hotel.getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Sadece kendi otelinize oda ekleyebilirsiniz!");
        }


        Room room = roomMapper.toEntity(request);


        room.setHotel(hotel);

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

        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Otel Bulunamadı! ID: " + hotelId);
        }

        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public RoomResponse updateRoom(Long id, RoomRequest request, String userEmail) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı! ID: " + id));


        if (!room.getHotel().getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu odayı güncelleme yetkiniz yok! Oda sizin otelinize ait değil.");
        }


        if (!room.getHotel().getId().equals(request.hotelId())) {
            Hotel newHotel = hotelRepository.findById(request.hotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yeni Otel Bulunamadı! ID: " + request.hotelId()));


            if (!newHotel.getManager().getEmail().equals(userEmail)) {
                throw new IllegalStateException("Odayı başkasının oteline taşıyamazsınız!");
            }
            room.setHotel(newHotel);
        }


        roomMapper.updateEntityFromRequest(request, room);

        Room updatedRoom = roomRepository.save(room);
        return roomMapper.toResponse(updatedRoom);
    }

    @Transactional
    @Override
    public void deleteRoom(Long id, String userEmail) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı!"));


        if (!room.getHotel().getManager().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Bu odayı silme yetkiniz yok! Oda sizin otelinize ait değil.");
        }


        roomRepository.delete(room);
    }
}