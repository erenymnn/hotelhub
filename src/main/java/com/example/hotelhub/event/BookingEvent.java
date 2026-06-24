package com.example.hotelhub.event;

import java.io.Serializable;

// Bu sınıfı oluştur
public record BookingEvent(
        Long bookingId,
        String customerEmail,
        String hotelName,
        String message
) implements Serializable {
    private static final long serialVersionUID = 1L;
}