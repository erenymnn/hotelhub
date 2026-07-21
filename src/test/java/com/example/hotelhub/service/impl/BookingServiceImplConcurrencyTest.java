package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.entity.enums.Role; // İŞTE DOĞRU IMPORT BURADA!
import com.example.hotelhub.exception.RoomAlreadyBookedException;
import com.example.hotelhub.messaging.producer.BookingProducer;
import com.example.hotelhub.repository.BookingRepository;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.repository.UserRepository;
import com.example.hotelhub.service.BookingService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class BookingServiceImplConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private BookingProducer bookingProducer;

    private Long dinamikOdaId;

    @BeforeEach
    void setUp() {
        cleanup();

        Hotel hotel = new Hotel();
        hotel.setName("Test Hilton");
        hotel = hotelRepository.save(hotel);

        Room room = new Room();
        room.setRoomNumber("101-Kral");
        room.setPricePerNight(BigDecimal.valueOf(1200));
        room.setHotel(hotel);
        room = roomRepository.save(room);

        dinamikOdaId = room.getId();

        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setEmail("test_user_" + i + "@hotelhub.com");
            user.setFirstName("Müşteri");
            user.setLastName(String.valueOf(i));
            user.setPassword("password123");

            // ASIL DÜZELTME: Senin Enum'ında USER yok, CUSTOMER var!
            user.setRoles(Collections.singleton(Role.CUSTOMER));
            userRepository.save(user);
        }
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createBooking_ShouldHandleConcurrentRequests_AndAllowOnlyOneBooking() throws InterruptedException {
        int toplamIstekSayisi = 5;

        ExecutorService executorService = Executors.newFixedThreadPool(toplamIstekSayisi);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger basariliIstekler = new AtomicInteger(0);
        AtomicInteger basarisizIstekler = new AtomicInteger(0);

        BookingRequest request = new BookingRequest(
                dinamikOdaId,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7)
        );

        for (int i = 0; i < toplamIstekSayisi; i++) {
            final String userEmail = "test_user_" + i + "@hotelhub.com";

            executorService.execute(() -> {
                try {
                    latch.await();
                    bookingService.createBooking(request, userEmail);
                    basariliIstekler.incrementAndGet();
                } catch (RoomAlreadyBookedException e) {
                    basarisizIstekler.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("!!! KİLİT İÇİNDE KRİTİK ÇÖKME YAŞANDI !!!");
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();

        Thread.sleep(3000);
        executorService.shutdown();

        System.out.println("=== KİLİT TEST LABORATUVARI SONUÇLARI ===");
        System.out.println("Toplam Atılan İstek: " + toplamIstekSayisi);
        System.out.println("Başarılı Rezervasyon Sayısı (Hedef 1): " + basariliIstekler.get());
        System.out.println("Engellenen Çifte Satış Sayısı (Hedef 4): " + basarisizIstekler.get());

        assertThat(basariliIstekler.get()).isEqualTo(1);
        assertThat(basarisizIstekler.get()).isEqualTo(toplamIstekSayisi - 1);
    }
}