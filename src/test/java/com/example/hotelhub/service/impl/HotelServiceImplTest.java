package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.HotelMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.UserRepository;
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
    private UserRepository userRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper; // MapStruct kullandığımız için bunu da sahte (mock) olarak veriyoruz

    @InjectMocks
    private HotelServiceImpl hotelService;



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
    @DisplayName("Başarılı Senaryo: Geçerli bilgilerle yeni otel oluşturulmalı")
    void createHotel_ShouldReturnHotelResponse_WhenUserExists() {
        // GIVEN
        String userEmail = "test@user.com";
        HotelRequest request = new HotelRequest("Hilton", "Istanbul", "Besiktas", "Adres", "05554443322", "info@hilton.com", "Açıklama", 5.0);

        User mockManager = new User();
        mockManager.setEmail(userEmail);

        Hotel mockHotel = new Hotel();
        Hotel savedHotel = new Hotel();
        savedHotel.setId(1L);
        savedHotel.setManager(mockManager);

        HotelResponse expectedResponse = new HotelResponse(1L, "Hilton", "Istanbul", "Besiktas", "Adres", "05554443322", "info@hilton.com", "Açıklama", 5.0, java.util.Collections.emptyList());

        when(userRepository.findByEmail(userEmail)).thenReturn(java.util.Optional.of(mockManager));
        when(hotelMapper.toEntity(request)).thenReturn(mockHotel);
        when(hotelRepository.save(mockHotel)).thenReturn(savedHotel);
        when(hotelMapper.toResponse(savedHotel)).thenReturn(expectedResponse);

        // WHEN
        HotelResponse result = hotelService.createHotel(request, userEmail);

        // THEN
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(hotelRepository, times(1)).save(mockHotel);
    }

    @Test
    @DisplayName("Güvenlik Senaryosu: Başkasının otelini güncellemeye çalışınca hata fırlatmalı")
    void updateHotel_ShouldThrowException_WhenUserIsNotManager() {
        // GIVEN
        Long hotelId = 1L;
        String requesterEmail = "hirsiz@user.com"; // Güncellemeyi deneyen kişi
        String realOwnerEmail = "sahip@user.com"; // Otelin gerçek sahibi

        HotelRequest request = new HotelRequest("Değişmiş Ad", "Istanbul", "Besiktas", "Adres", "05554443322", "info@hilton.com", "Açıklama", 5.0);

        User realOwner = new User();
        realOwner.setEmail(realOwnerEmail);

        Hotel mockHotel = new Hotel();
        mockHotel.setId(hotelId);
        mockHotel.setManager(realOwner);

        when(hotelRepository.findById(hotelId)).thenReturn(java.util.Optional.of(mockHotel));

        // WHEN & THEN
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> hotelService.updateHotel(hotelId, request, requesterEmail)
        );

        assertEquals("Bu oteli güncelleme yetkiniz yok! Sadece kendi otelinizi güncelleyebilirsiniz.", exception.getMessage());

        // KRİTİK: Yetkisiz işlem olduğu için save() METODU ASLA ÇAĞRILMAMALI! (Güvenlik Testi)
        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void deleteHotel() {
    }
}