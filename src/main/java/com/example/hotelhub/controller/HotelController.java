package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;


    @GetMapping
    public ResponseEntity<Page<HotelResponse>> getAllHotels(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(hotelService.getAllHotels(pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }
//burada search normalde get ile yazılır ama url kirletmemek adına post istegi atarken body kısmına istedigin search anahtar kelimeyi yazarak hem istekler daha güvenli ve temiz body şeklinde işimiz daha kolaylaşır.


    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<HotelResponse> create(
            @Valid @RequestBody HotelRequest request,
            Principal principal
    ) {
        String userEmail = principal.getName();
        return new ResponseEntity<>(hotelService.createHotel(request, userEmail), HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequest request,
            Principal principal
    ) {
        String userEmail = principal.getName();
        return ResponseEntity.ok(hotelService.updateHotel(id, request, userEmail));
    }


    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(
            @PathVariable Long id,
            Principal principal
    ) {
        String userEmail = principal.getName();
        hotelService.deleteHotel(id, userEmail);
        return ResponseEntity.noContent().build();

    }
}