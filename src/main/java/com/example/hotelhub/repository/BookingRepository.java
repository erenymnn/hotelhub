package com.example.hotelhub.repository;

import com.example.hotelhub.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    //Bu sorgu seçilen odanın istenen tarihler arasında onaylı başka bir rezervasyonla çakışıp çakışmadığını, uzun if-else bloklarına gerek kalmadan doğrudan veritabanı seviyesinde hızlıca tespit etmemizi sağlar.
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkInDate < :checkOutDate " +
            "AND b.checkOutDate > :checkInDate")
    boolean existsConflictingBooking(@Param("roomId") Long roomId,
                                     @Param("checkInDate") LocalDate checkInDate,
                                     @Param("checkOutDate") LocalDate checkOutDate);

    List<Booking> findByUserEmail(String email);
}
