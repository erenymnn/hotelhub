package com.example.hotelhub.repository;

import com.example.hotelhub.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {


    @Modifying
    @Query("update Room r set r.deleted = true where r.hotel.id = :hotelId") //sadece sistemden gizleniyor.
    int softDeleteByHotelId(@Param("hotelId") Long hotelId);

    // RoomRepository içine şu metodu ekle:
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.deleted = false")
    List<Room> findAvailableRoomsByHotelId(@Param("hotelId") Long hotelId);

    List<Room> findByHotelId(Long hotelId);
}
