package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import com.example.hotelhub.entity.User;
import com.example.hotelhub.exception.ResourceNotFoundException;
import com.example.hotelhub.mapper.RoomMapper;
import com.example.hotelhub.repository.HotelRepository;
import com.example.hotelhub.repository.RoomRepository;
import com.example.hotelhub.service.RedisCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Cache cache;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Hotel mockHotel;
    private Room mockRoom;
    private User mockManager;
    private RoomRequest roomRequest;
    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        mockManager = new User();
        mockManager.setEmail("manager@hotel.com");

        mockHotel = new Hotel();
        mockHotel.setId(1L);
        mockHotel.setName("Test Hotel");
        mockHotel.setManager(mockManager);

        mockRoom = new Room();
        mockRoom.setId(10L);
        mockRoom.setRoomNumber("101");
        mockRoom.setHotel(mockHotel);

        roomRequest = new RoomRequest("101", null, "Sea", BigDecimal.valueOf(100.0), 2, true, true, Collections.emptyList(), 1L);
        roomResponse = new RoomResponse(10L, "101", null, "Sea", BigDecimal.valueOf(100.0), 2, true, true, Collections.emptyList(), true);

        org.springframework.test.util.ReflectionTestUtils.setField(roomService, "self", roomService);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addRoomToHotel_WhenUserIsManager_ShouldAddSuccessfully() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(mockHotel));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        
        when(roomMapper.toEntity(any(RoomRequest.class))).thenReturn(mockRoom);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);
        when(roomMapper.toResponse(any(Room.class))).thenReturn(roomResponse);

        RoomResponse response = roomService.addRoomToHotel(roomRequest, "manager@hotel.com");

        assertNotNull(response);
        assertEquals("101", response.roomNumber());
        verify(redisCacheService, times(1)).saveWithJitter(eq("roomDetails::10"), eq(roomResponse));
    }

    @Test
    void addRoomToHotel_WhenUserIsNotManager_ShouldThrowAccessDenied() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(mockHotel));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        assertThrows(AccessDeniedException.class, 
            () -> roomService.addRoomToHotel(roomRequest, "hacker@test.com"));
    }

    @Test
    void deleteRoom_WhenUserIsManager_ShouldSoftDeleteAndEvictCache() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(mockRoom));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
        when(cacheManager.getCache("roomsByHotel")).thenReturn(cache);

        roomService.deleteRoom(10L, "manager@hotel.com");

        assertTrue(mockRoom.isDeleted());
        verify(roomRepository, times(1)).save(mockRoom);
        verify(cache, times(1)).evict(1L);
    }

    @Test
    void getRoomById_WhenExists_ShouldReturnRoomResponse() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(mockRoom));
        when(roomMapper.toResponse(mockRoom)).thenReturn(roomResponse);

        RoomResponse response = roomService.getRoomById(10L);

        assertNotNull(response);
        assertEquals(10L, response.id());
    }

    @Test
    void getRoomById_WhenNotExists_ShouldThrowResourceNotFound() {
        when(roomRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomById(10L));
    }
}
