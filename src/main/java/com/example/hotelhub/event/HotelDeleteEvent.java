package com.example.hotelhub.event;

import java.io.Serializable;

public record HotelDeleteEvent(Long hotelId) implements Serializable {
    private static final long serialVersionUID = 1L;
}