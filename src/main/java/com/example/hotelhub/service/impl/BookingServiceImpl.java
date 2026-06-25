package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.Booking;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.BookingStatus;
import com.example.hotelhub.entity.enums.Role;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.List;

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

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request, String userEmail) {
        validateBookingDates(request);

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı!"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

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

        //Event fırlat (Asenkron bildirim için)
        BookingEvent event = new BookingEvent(
                savedBooking.getId(),
                user.getEmail(),
                savedBooking.getHotelName(),
                "Rezervasyonunuz başarıyla onaylandı!"
        );
        bookingProducer.sendBookingNotification(event);


        return bookingMapper.toResponse(savedBooking);
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

    private void assertBookingOwnership(Booking booking, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));


        boolean isAdmin = user.getRoles().contains(Role.ADMIN);

        //  Rezervasyonun sahibi kim?
        String bookingOwnerEmail = booking.getUser() == null ? null : booking.getUser().getEmail();

        // Eğer ADMIN değilse VE rezervasyonun sahibi değilse hata fırlat
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
