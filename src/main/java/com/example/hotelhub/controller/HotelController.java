package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    // 1. HERKESE AÇIK: Müşteriler veya sisteme üye olmayanlar otelleri görebilmeli
    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    // 2. HERKESE AÇIK: Otel detayını herkes görebilir
    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    // 3. SADECE YETKİLİLER: Otel ekleme. İşlemi yapanın e-postasını servise gönderiyoruz ki oteli ona zimmetleyelim.
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<HotelResponse> create(
            @Valid @RequestBody HotelRequest request,
            Principal principal
    ) {
        String userEmail = principal.getName();
        return new ResponseEntity<>(hotelService.createHotel(request, userEmail), HttpStatus.CREATED);
    }

    // 4. SADECE YETKİLİLER: Güncelleme işlemi. Servis katmanında "Bu otel bu e-postaya mı ait?" diye kontrol edilecek.
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

    // 5. SADECE YETKİLİLER: Silme işlemi. Yine Servis katmanında sahiplik kontrolü yapılacak.
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(
            @PathVariable Long id,
            Principal principal
    ) {
        String userEmail = principal.getName();
        hotelService.deleteHotel(id, userEmail);
        return ResponseEntity.noContent().build();
        // oteli sildim Ama sana geri döndürebileceğim bir veri (Content) kalmadı,
        // bu yüzden boş dönüyor anlamına gelir.
        // .build() ise Spring Boot'un ResponseEntity nesnesi oluşturmasını sağlar.
    }
}