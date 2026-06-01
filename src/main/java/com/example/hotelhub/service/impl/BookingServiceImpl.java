package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.Booking;
import com.example.hotelhub.entity.enums.BookingStatus;
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

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı!"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));


        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new IllegalArgumentException("Çıkış tarihi, giriş tarihinden sonra olmalıdır!");
        }
        boolean isOccupied = bookingRepository.existsConflictingBooking(
                request.roomId(), request.checkInDate(), request.checkOutDate());

        if (isOccupied) {
            throw new RoomAlreadyBookedException("Seçtiğiniz tarihlerde bu oda maalesef doludur!");
        }


        long daysBetween = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(daysBetween));


        Booking booking = bookingMapper.toEntity(request);

        booking.setRoom(room);
        booking.setUser(user);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);


        return bookingMapper.toResponse(savedBooking);

    }

    @Override
    public List<BookingResponse> getUserBookings(String userEmail) {

        List<Booking> bookings = bookingRepository.findByUserEmail(userEmail);


        return bookings.stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    public void cancelBooking(Long bookingId, String userEmail) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("error.hotel.notfound"));


        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Sadece kendi rezervasyonunuzu iptal edebilirsiniz!");
        }


        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalStateException("Bu rezervasyon zaten iptal edilmiş!");
        }


        if (!java.time.LocalDate.now().isBefore(booking.getCheckInDate())) {
            throw new IllegalStateException("Süresi geçmiş veya başlamış rezervasyonlar iptal edilemez!");
        }


        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);
    }
}
