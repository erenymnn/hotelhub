package com.example.hotelhub.repository;

import com.example.hotelhub.entity.Hotel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class HotelRepositoryTest {
    @Autowired
    private HotelRepository hotelRepository;

    @Test
    @DisplayName("Veritabanına otel kaydedilebilmeli ve ID ile geri bulunabilmeli")
    void shouldSaveAndFindHotel() {
        // 1. GIVEN (Hazırlık)
        Hotel hotel = new Hotel();
        hotel.setName("Test Integration Hotel");
        hotel.setCity("Istanbul");
        hotel.setDistrict("Kadikoy");
        hotel.setAddress("Test Adres 123");
        hotel.setNumber("05554443322");
        hotel.setEmail("test@hotel.com");
        hotel.setDescription("Test Açıklaması");
        hotel.setRating(5.0);

        // 2. WHEN (Eylem - H2 RAM Veritabanına Kayıt)
        Hotel savedHotel = hotelRepository.save(hotel);
        Optional<Hotel> foundHotel = hotelRepository.findById(savedHotel.getId());

        // 3. THEN (Doğrulama - Veritabanından doğru geldi mi?)
        assertTrue(foundHotel.isPresent(), "Otel veritabanında bulunamadı!");
        assertEquals("Test Integration Hotel", foundHotel.get().getName());
        assertEquals("Istanbul", foundHotel.get().getCity());
        assertTrue(savedHotel.getId() > 0, "Kaydedilen otel veritabanından geçerli bir ID almalı");
    }
}
