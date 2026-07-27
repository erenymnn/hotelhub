package com.example.hotelhub.repository;

import com.example.hotelhub.entity.Booking;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("bookinguser@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("pass");
        entityManager.persist(testUser);

        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setDistrict("Test District");
        testHotel.setDescription("Test Desc");
        entityManager.persist(testHotel);

        testRoom = new Room();
        testRoom.setRoomNumber("101");
        testRoom.setHotel(testHotel);
        testRoom.setCapacity(2);
        testRoom.setPricePerNight(BigDecimal.valueOf(100));
        testRoom.setIsAvailable(true);
        entityManager.persist(testRoom);
        entityManager.flush();
    }

    @Test
    void existsConflictingBooking_WhenConflictExists_ShouldReturnTrue() {
        Booking booking = new Booking();
        booking.setRoom(testRoom);
        booking.setUser(testUser);
        booking.setCheckInDate(LocalDate.of(2023, 10, 10));
        booking.setCheckOutDate(LocalDate.of(2023, 10, 15));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(BigDecimal.valueOf(500));
        entityManager.persistAndFlush(booking);

        boolean isConflicting = bookingRepository.existsConflictingBooking(
                testRoom.getId(),
                LocalDate.of(2023, 10, 12),
                LocalDate.of(2023, 10, 17)
        );

        assertTrue(isConflicting);
    }

    @Test
    void existsConflictingBooking_WhenNoConflict_ShouldReturnFalse() {
        Booking booking = new Booking();
        booking.setRoom(testRoom);
        booking.setUser(testUser);
        booking.setCheckInDate(LocalDate.of(2023, 10, 10));
        booking.setCheckOutDate(LocalDate.of(2023, 10, 15));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(BigDecimal.valueOf(500));
        entityManager.persistAndFlush(booking);

        boolean isConflicting = bookingRepository.existsConflictingBooking(
                testRoom.getId(),
                LocalDate.of(2023, 10, 16),
                LocalDate.of(2023, 10, 20)
        );

        assertFalse(isConflicting);
    }

    @Test
    void findByUserEmail_ShouldReturnBookings() {
        Booking booking = new Booking();
        booking.setRoom(testRoom);
        booking.setUser(testUser);
        booking.setCheckInDate(LocalDate.of(2023, 10, 10));
        booking.setCheckOutDate(LocalDate.of(2023, 10, 15));
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(BigDecimal.valueOf(500));
        entityManager.persistAndFlush(booking);

        List<Booking> bookings = bookingRepository.findByUserEmail("bookinguser@test.com");

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertEquals("bookinguser@test.com", bookings.get(0).getUser().getEmail());
    }
}
