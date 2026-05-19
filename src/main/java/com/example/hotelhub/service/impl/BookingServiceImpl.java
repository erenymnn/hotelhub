package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.Booking;
import com.example.hotelhub.entity.BookingStatus;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.exception.RoomAlreadyBookedException;
import com.example.hotelhub.mapper.BookingMapper;
import com.example.hotelhub.repository.BookingRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;


    @Override
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
//   ODA VE KULLANICIYI BUL yksa hata fırlat
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı!"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        //  Çıkış tarihi, giriş tarihinden önce veya aynı gün olamaz
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new IllegalArgumentException("Çıkış tarihi, giriş tarihinden sonra olmalıdır!");
        }
        boolean isOccupied = bookingRepository.existsConflictingBooking(
                request.roomId(), request.checkInDate(), request.checkOutDate());

        if (isOccupied) {
            throw new RoomAlreadyBookedException("Seçtiğiniz tarihlerde bu oda maalesef doludur!");
        }

        //Kalınacak gün sayısını bul ve gecelik fiyatla çarp
        long daysBetween = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(daysBetween));


        // MapStruct bizim için tarihleri otomatik olarak request'ten alıp Entity'ye yerleştirecek
        Booking booking = bookingMapper.toEntity(request);
        // Geriye kalan İş Mantığı kısımlarını biz setliyoruz
        booking.setRoom(room);
        booking.setUser(user);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        //  RESPONSE DÖN - MapStruct ile
        return bookingMapper.toResponse(savedBooking);

    }

    @Override
    public List<BookingResponse> getUserBookings(String userEmail) {
        // 1. Kullanıcının tüm rezervasyonlarını veritabanından çek
        List<Booking> bookings = bookingRepository.findByUserEmail(userEmail);

        // 2. MapStruct kullanarak gelen Entity listesini DTO listesine çevir ve dön
        return bookings.stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    public void cancelBooking(Long bookingId, String userEmail) {
        // 1. Rezervasyonu bul
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon bulunamadı!"));

        // 2. GÜVENLİK KONTROLÜ: Bu rezervasyon işlemi yapan kullanıcıya mı ait?
        if (!booking.getUser().getEmail().equals(userEmail)) {
            // Spring Security'nin AccessDeniedException sınıfını da kullanabiliriz ama
            // şimdilik basit bir RuntimeException fırlatıyoruz.
            throw new IllegalStateException("Sadece kendi rezervasyonunuzu iptal edebilirsiniz!");
        }

        // 3. PROFESYONEL YAKLAŞIM: Veritabanından satırı silmek (delete) yerine
        // durumunu CANCELED yapıyoruz. Böylece otel sahibi iptal edilen rezervasyonların geçmişini görebilir.
        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);
    }
}
