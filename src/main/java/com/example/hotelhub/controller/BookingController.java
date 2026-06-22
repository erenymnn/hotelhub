package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Booking Controller", description = "Rezervasyon oluşturma ve sorgulama işlemleri")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal
    ) {

        String userEmail = principal.getName();


        return new ResponseEntity<>(bookingService.createBooking(request, userEmail), HttpStatus.CREATED);
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getUserBookings(Principal principal) {

        String userEmail = principal.getName();


        return ResponseEntity.ok(bookingService.getUserBookings(userEmail));
    }


    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long bookingId,
            Principal principal
    ) {

        String userEmail = principal.getName();


        bookingService.cancelBooking(bookingId, userEmail);


        return ResponseEntity.ok("Rezervasyon başarıyla iptal edildi.");
    }
}
