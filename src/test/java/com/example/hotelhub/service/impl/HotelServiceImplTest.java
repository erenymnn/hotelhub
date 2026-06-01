package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.HotelMapper;
import com.example.hotelhub.repository.HotelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper; // MapStruct kullandığımız için bunu da sahte (mock) olarak veriyoruz

    @InjectMocks
    private HotelServiceImpl hotelService;

    @Test
    void createHotel() {
    }

    @Test
    void getAllHotels() {
    }

    @Test
    void searchHotels() {
    }

    @Test
    @DisplayName("Başarılı Senaryo: ID bulunduğunda HotelResponse dönmeli")
    void getHotelById_ShouldReturnHotelResponse_WhenHotelExists() {
        // 1. GIVEN (Verilenler / Hazırlık)
        Long hotelId = 1L;

        Hotel mockHotel = new Hotel();
        mockHotel.setId(hotelId);

        // Record olduğu için tüm parametreleri (sendeki sıraya göre) dolduruyoruz
        HotelResponse mockResponse = new HotelResponse(
                hotelId,
                "Hilton Istanbul",
                "Istanbul",
                "Besiktas",
                "Test Adresi 123",
                "05555555555",
                "info@hilton.com",
                "Harika bir test oteli",
                5.0,
                java.util.Collections.emptyList() // Şimdilik odalar listesi boş olsun
        );// Eğer record kullanıyorsan uygun şekilde new'le

        // Veritabanını kandırıyoruz: "findById(1L) çağrılırsa mockHotel dön"
        when(hotelRepository.findById(hotelId)).thenReturn(java.util.Optional.of(mockHotel));

        // Mapper'ı kandırıyoruz: "toResponse(mockHotel) çağrılırsa mockResponse dön"
        when(hotelMapper.toResponse(mockHotel)).thenReturn(mockResponse);

        // 2. WHEN (Eylem / Test edilen metodu çağırma)
        HotelResponse result = hotelService.getHotelById(hotelId);

        // 3. THEN (Sonuç / Doğrulama)
        assertNotNull(result);
        assertEquals(mockResponse, result); // Dönen sonucun bizim mockResponse olup olmadığını kontrol ediyoruz

        // Repository ve Mapper'ın gerçekten tam 1 kere çağrıldığını teyit ediyoruz (Performans ve güvenlik testi)
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(hotelMapper, times(1)).toResponse(mockHotel);
    }

    @Test
    @DisplayName("Hata Senaryosu: ID bulunamadığında Exception fırlatmalı")
    void getHotelById_ShouldThrowException_WhenHotelDoesNotExist() {
        // 1. GIVEN (Verilenler)
        Long hotelId = 99L; // Olmayan bir ID uyduruyoruz

        // Veritabanı boş dönüyor (Optional.empty)
        when(hotelRepository.findById(hotelId)).thenReturn(java.util.Optional.empty());

        // 2 & 3. WHEN & THEN (Hata fırlatmasını bekliyoruz)
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> hotelService.getHotelById(hotelId)
        );

        // Fırlatılan hatanın mesajı doğru mu diye kontrol ediyoruz
        assertEquals("Otel Bulunamadı! ID: " + hotelId, exception.getMessage());

        // Veritabanına gidilmiş mi diye bakıyoruz
        verify(hotelRepository, times(1)).findById(hotelId);

        // ÇOK KRİTİK: Otel bulunamadığı için Mapper HİÇ ÇAĞRILMAMIŞ OLMALI!
        verify(hotelMapper, never()).toResponse(any());
    }

    @Test
    void updateHotel() {
    }

    @Test
    void deleteHotel() {
    }
}