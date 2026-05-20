package com.example.hotelhub.dto.request;

import com.example.hotelhub.entity.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record RoomRequest(
        @NotBlank(message = "Oda numarası boş olamaz!")
        String roomNumber,

        @NotNull(message = "Oda tipi boş olamaz!")
        RoomType type,

        @NotBlank(message = "Manzara tipi boş bırakılamaz!")
        String viewType, // Validasyon eklendi

        @NotNull(message = "Fiyat boş olamaz!")
        @Positive(message = "Fiyat sıfırdan büyük olmalıdır!")
        BigDecimal pricePerNight,

        @NotNull(message = "Kapasite boş olamaz!")
        @Min(value = 1, message = "Kapasite en az 1 kişi olmalıdır!")
        Integer capacity,

        // Entity'deki boolean'lar buraya da eklendi
        Boolean hasAirConditioning,
        Boolean hasBalcony,

        List<String> features,

        @NotNull(message = "Oda bir otele ait olmalıdır (Hotel Id Gerekli)!")
        Long hotelId
) {
}
