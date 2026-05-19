package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    // Güvenlik detayı: userEmail'i dışarıdan (istekten) değil, token'dan alıp buraya göndereceğiz!
    BookingResponse createBooking(BookingRequest request, String userEmail);

    // Kullanıcının kendi rezervasyonlarını listelemesi için
    List<BookingResponse> getUserBookings(String userEmail);

    // Kullanıcının kendi rezervasyonunu iptal etmesi için
    void cancelBooking(Long bookingId, String userEmail);
}
