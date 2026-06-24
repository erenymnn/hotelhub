package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.service.RoomService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Controller", description = "Oda yönetimi ve otel içi oda işlemleri")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHotelId(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId));
    }

    // hasAnyRole yerine hasAnyAuthority kullanıyoruz (Güvenlik tutarlılığı için)
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(
            @Valid @RequestBody RoomRequest request,
            Principal principal
    ) {
        // principal.getName() direkt metodun içine gönderiyoruz, daha temiz
        return new ResponseEntity<>(roomService.addRoomToHotel(request, principal.getName()), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(roomService.updateRoom(id, request, principal.getName()));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            Principal principal
    ) {
        roomService.deleteRoom(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    //Müsaitlik durumu güncelleme
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Void> setRoomAvailability(
            @PathVariable Long id,
            @RequestParam boolean isAvailable,
            Principal principal
    ) {
        roomService.setRoomAvailability(id, isAvailable, principal.getName());
        return ResponseEntity.ok().build();
    }
}