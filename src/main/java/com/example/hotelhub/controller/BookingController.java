package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.enums.BookingStatus;
import com.example.hotelhub.service.BookingService;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "Rezervasyon işlemleri")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal // principal.getName() ile email'i alıyoruz
    ) {
        return new ResponseEntity<>(bookingService.createBooking(request, principal.getName()), HttpStatus.CREATED);
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> getUserBookings(Principal principal) { //principal ile "o an giriş yapmış kişiye" ait verileri çekeriz
        return ResponseEntity.ok(bookingService.getUserBookings(principal.getName()));
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            Principal principal //principal alma sebebimiz eğer eposta alsaydık kotu biri oepostayı yazıp rezervasyonu iptal ederdi.
    //principal kimlik kartıdır.
    ) {
        bookingService.cancelBooking(bookingId, principal.getName());
        return ResponseEntity.noContent().build(); // 204 No Content, silme/iptal işlemleri için standarttır
    }

    // YENİ EKLENEN METOT: Admin/Manager yetkisi gerektirir
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status
    ) {
        bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok().build();
    }
}