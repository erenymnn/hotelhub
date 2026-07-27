package com.example.hotelhub.repository;

import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setCity("City");
        testHotel.setDistrict("District");
        testHotel.setDescription("Desc");
        entityManager.persistAndFlush(testHotel);
    }

    @Test
    void softDeleteByHotelId_ShouldMarkRoomsAsDeleted() {
        Room room1 = createRoom("101", false);
        Room room2 = createRoom("102", false);

        int updatedCount = roomRepository.softDeleteByHotelId(testHotel.getId());
        entityManager.clear(); // Clear cache to force DB read

        assertEquals(2, updatedCount);
        
        java.util.Optional<Room> fetchedRoom = roomRepository.findById(room1.getId());
        org.junit.jupiter.api.Assertions.assertTrue(fetchedRoom.isEmpty());
    }

    @Test
    void findAvailableRoomsByHotelId_ShouldReturnOnlyNotDeletedAndAvailable() {
        Room roomAvailable = createRoom("101", false);
        roomAvailable.setIsAvailable(true);
        entityManager.persistAndFlush(roomAvailable);

        Room roomDeleted = createRoom("102", true);
        roomDeleted.setIsAvailable(true);
        entityManager.persistAndFlush(roomDeleted);

        Room roomNotAvailable = createRoom("103", false);
        roomNotAvailable.setIsAvailable(false);
        entityManager.persistAndFlush(roomNotAvailable);

        List<Room> availableRooms = roomRepository.findAvailableRoomsByHotelId(testHotel.getId());

        assertEquals(1, availableRooms.size());
        assertEquals("101", availableRooms.get(0).getRoomNumber());
    }

    private Room createRoom(String roomNumber, boolean deleted) {
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setHotel(testHotel);
        room.setCapacity(2);
        room.setPricePerNight(BigDecimal.valueOf(100));
        room.setDeleted(deleted);
        return entityManager.persist(room);
    }
}
