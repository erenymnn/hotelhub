package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.Booking;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.BookingStatus;
import com.example.hotelhub.event.BookingEvent;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.exception.RoomAlreadyBookedException;
import com.example.hotelhub.mapper.BookingMapper;
import com.example.hotelhub.messaging.producer.BookingProducer;
import com.example.hotelhub.repository.BookingRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) //örneğin mail gönderimi patlarsa veya veritabanı bağlantısı koparsa), yapılan tüm değişiklikler otomatik geri alınır. Verin asla bozulmaz.
//eğer true yazmazsak tüm nesneleri takip edip güncelleme var mı diye bakar .
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;
    private final BookingProducer bookingProducer;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final RedissonClient redissonClient;
    private final CacheManager cacheManager;

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        validateBookingDates(request);

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı!"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        // KİLİT ANAHTARINI OLUŞTUR (Sadece bu oda ID'sine özel bir kilit)
        String lockKey = "lock::room::" + request.roomId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            //  KİLİDİ ALMAYI DENE (Maks 5 saniye bekle, kilidi alırsan 10 saniye sende kalsın)
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!isLocked) {
                // Kilit alınamazsa (başka biri şu an bu odayı alıyorsa) işlemi reddet!
                log.warn("Oda {} için kilit alınamadı! Başka bir işlem devam ediyor.", request.roomId());
                throw new RoomAlreadyBookedException("Bu oda şu an başka bir müşteri tarafından rezerve ediliyor. Lütfen birazdan tekrar deneyin!");
            }

            log.info("Oda {} için kilit BAŞARIYLA ALINDI. Rezervasyon işlemi başlıyor...", request.roomId());

            // Eğer kilit bizdeyse, artık güvenle veritabanına sorabiliriz. Çakışma ihtimali SIFIR.
            if (bookingRepository.existsConflictingBooking(request.roomId(), request.checkInDate(), request.checkOutDate())) {
                throw new RoomAlreadyBookedException("Seçtiğiniz tarihlerde bu oda maalesef doludur!");
            }

            long daysBetween = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
            BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(daysBetween));

            Booking booking = bookingMapper.toEntity(request);
            booking.setRoom(room);
            booking.setUser(user);
            booking.setTotalPrice(totalPrice);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setHotelName(room.getHotel().getName());

            Booking savedBooking = bookingRepository.save(booking);

            // CACHE INVALIDATION
            // Odayı sattık, otelin oda listesindeki durum bayatladı. Hemen o otelin listesini çöpe atıyoruz!
            Long hotelId = room.getHotel().getId();
            // Güvenli Cache Temizliği
            var cache = cacheManager.getCache("roomsByHotel");
            if (cache != null) {
                cache.evict(hotelId);
            }
            log.info("Otel {} için oda listesi cache'i temizlendi.", hotelId);

            // Event fırlat (Asenkron bildirim için)
            BookingEvent event = new BookingEvent(
                    savedBooking.getId(),
                    user.getEmail(),
                    savedBooking.getHotelName(),
                    "Rezervasyonunuz başarıyla onaylandı!"
            );
            bookingProducer.sendBookingNotification(event);

            return bookingMapper.toResponse(savedBooking);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Kilit beklenirken hata oluştu: ", e);
            throw new IllegalStateException("Sistem yoğunluğu nedeniyle işleminiz gerçekleştirilemedi.");
        } finally {
            // KİLİDİ SERBEST BIRAK
            // İşlem ister başarılı olsun ister hata fırlatsın, kapıyı diğer müşteriler için geri açıyoruz.
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Oda {} için kilit serbest bırakıldı.", request.roomId());
            }
        }
    }
    @Transactional
    @Override
    public void updateBookingStatus(Long id, BookingStatus status) {
        Booking booking = findBookingById(id);
        booking.setStatus(status);
        bookingRepository.save(booking);
        log.info("Rezervasyon durumu güncellendi. ID: {}, Yeni Durum: {}", id, status);
    }
    @Override
    public List<BookingResponse> getUserBookings(String userEmail) {
        return bookingRepository.findByUserEmail(userEmail)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = findBookingById(bookingId);
        assertBookingOwnership(booking, userEmail);

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalStateException("Bu rezervasyon zaten iptal edilmiş!");
        }

        if (!LocalDate.now().isBefore(booking.getCheckInDate())) {
            throw new IllegalStateException("Süresi geçmiş veya başlamış rezervasyonlar iptal edilemez!");
        }

        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon bulunamadı! ID: " + bookingId));
    }

    // Eski, DB'ye inen metodu silip yerine bunu yapıştırıyoruz:
    private void assertBookingOwnership(Booking booking, String userEmail) {

        //Veritabanına (userRepository'e) HİÇ İNMEDEN hafızadaki token yetkilerini alıyoruz
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // JWT'den gelen roller arasında ADMIN var mı kontrol ediyoruz
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));

        // Rezervasyonun asıl sahibinin kim olduğunu alıyoruz
        String bookingOwnerEmail = booking.getUser() == null ? null : booking.getUser().getEmail();

        // Eğer ADMIN değilse VE işlemi yapan kişi rezervasyonun sahibi değilse hata fırlat
        if (!isAdmin && !Objects.equals(bookingOwnerEmail, userEmail)) {
            throw new AccessDeniedException("Sadece kendi rezervasyonunuzu iptal edebilirsiniz!");
        }
    }
    private void validateBookingDates(BookingRequest request) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new IllegalArgumentException("Çıkış tarihi, giriş tarihinden sonra olmalıdır!");
        }
    }
}
