package com.example.hotelhub.dto.response;

import java.util.List;

public record HotelResponse(
        // (Kullanıcıya Döneceğimiz Cevap)
        Long id,
        String name,
        String city,
        String address,
        Double rating ,
        List<RoomResponse> rooms // EKLENEN YENİ SATIR: Artık otel, odalarını da taşıyacak!
) {
}
