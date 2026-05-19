package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal // Spring Security, o an giriş yapmış kullanıcının bilgilerini buraya otomatik koyar!
    ) {
        // Token'ın içinden kullanıcının email adresini (username) çekiyoruz
        String userEmail = principal.getName();

        // Servise isteği ve email'i gönderip rezervasyonu oluşturuyoruz
        return new ResponseEntity<>(bookingService.createBooking(request, userEmail), HttpStatus.CREATED);
    }
    // 1. KULLANICININ KENDİ REZERVASYONLARINI GÖRMESİ
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getUserBookings(Principal principal) {
        // Token'dan e-postayı alıyoruz
        String userEmail = principal.getName();

        // Servise gönderip listeyi dönüyoruz
        return ResponseEntity.ok(bookingService.getUserBookings(userEmail));
    }

    // 2. KULLANICININ KENDİ REZERVASYONUNU İPTAL ETMESİ
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long bookingId,
            Principal principal
    ) {
        // Token'dan e-postayı alıyoruz
        String userEmail = principal.getName();

        // İptal işlemini servise devrediyoruz
        bookingService.cancelBooking(bookingId, userEmail);

        // İşlem başarılıysa mesaj dönüyoruz
        return ResponseEntity.ok("Rezervasyon başarıyla iptal edildi.");
    }
}
