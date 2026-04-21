package com.example.hotelhub.dto.response;

public record HotelResponse(
      // (Kullanıcıya Döneceğimiz Cevap)
      Long id,
      String name,
      String city,
      String address,
      Double rating

) {
}
