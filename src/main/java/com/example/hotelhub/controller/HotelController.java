package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.service.HotelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotel Controller", description = "Otel yönetimi işlemleri")
public class HotelController {

    private final HotelService hotelService;

    // 1. Genel Liste (Halka açık)
    @GetMapping
    public ResponseEntity<Page<HotelResponse>> getAllHotels(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(hotelService.getAllHotels(pageable));
    }

    // 2. Arama (Halka açık - POST ile karmaşık filtreleme)
    @PostMapping("/search")
    public ResponseEntity<Page<HotelResponse>> searchHotels(
            @RequestBody HotelSearchRequest request,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(hotelService.searchHotels(request, pageable));
    }

    // 3. Manager'ın kendi otelleri (Güvenli)
    @GetMapping("/my-hotels")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<Page<HotelResponse>> getMyHotels(
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            Principal principal
    ) {
        return ResponseEntity.ok(hotelService.getMyHotels(principal.getName(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<HotelResponse> create(
            @Valid @RequestBody HotelRequest request,
            Principal principal
    ) {
        return new ResponseEntity<>(hotelService.createHotel(request, principal.getName()), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(hotelService.updateHotel(id, request, principal.getName()));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(
            @PathVariable Long id,
            Principal principal
    ) {
        hotelService.deleteHotel(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}