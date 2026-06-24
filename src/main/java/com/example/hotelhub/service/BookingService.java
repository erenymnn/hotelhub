package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.enums.BookingStatus;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request, String userEmail);


    List<BookingResponse> getUserBookings(String userEmail);


    void cancelBooking(Long bookingId, String userEmail);

    void updateBookingStatus(Long id, BookingStatus status);
}
