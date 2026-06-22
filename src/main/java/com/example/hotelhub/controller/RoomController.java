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

    // HERKESE AÇIK
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // HERKESE AÇIK
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHotelId(@PathVariable Long hotelId) {
        return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId));
    }

    // SADECE YETKİLİLER
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(
            @Valid @RequestBody RoomRequest request,
            Principal principal
    ) {
        String userEmail = principal.getName();
        return new ResponseEntity<>(roomService.addRoomToHotel(request, userEmail), HttpStatus.CREATED);
    }

    // SADECE YETKİLİLER
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request,
            Principal principal
    ) {
        String userEmail = principal.getName();
        return ResponseEntity.ok(roomService.updateRoom(id, request, userEmail));
    }

    // SADECE YETKİLİLER
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            Principal principal
    ) {
        String userEmail = principal.getName();
        roomService.deleteRoom(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}