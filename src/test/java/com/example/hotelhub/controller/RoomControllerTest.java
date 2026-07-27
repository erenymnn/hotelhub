package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = RoomController.class,
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class, org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class},
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.example.hotelhub.config.SecurityConfig.class,
            com.example.hotelhub.config.JwtAuthenticationFilter.class,
            com.example.hotelhub.config.RateLimitFilter.class
        }
    )
)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        roomResponse = new RoomResponse(10L, "101", null, "Sea", BigDecimal.valueOf(150.0), 2, true, true, List.of(), true);
    }

    @Test
    void getRoomById_ShouldReturn200() throws Exception {
        when(roomService.getRoomById(10L)).thenReturn(roomResponse);

        mockMvc.perform(get("/api/rooms/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));
    }

    @Test
    void getRoomsByHotelId_ShouldReturn200AndList() throws Exception {
        when(roomService.getRoomsByHotelId(1L)).thenReturn(List.of(roomResponse));

        mockMvc.perform(get("/api/rooms/hotel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("101"));
    }

    @Test
    void addRoom_ShouldReturn201() throws Exception {
        RoomRequest request = new RoomRequest("101", com.example.hotelhub.entity.enums.RoomType.SINGLE, "Sea", BigDecimal.valueOf(150.0), 2, true, true, List.of(), 1L);
        when(roomService.addRoomToHotel(any(RoomRequest.class), anyString())).thenReturn(roomResponse);

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> "manager@test.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("101"));
    }

    @Test
    void updateRoom_ShouldReturn200() throws Exception {
        RoomRequest request = new RoomRequest("101", com.example.hotelhub.entity.enums.RoomType.SINGLE, "Sea", BigDecimal.valueOf(150.0), 2, true, true, List.of(), 1L);
        when(roomService.updateRoom(eq(10L), any(RoomRequest.class), anyString())).thenReturn(roomResponse);

        mockMvc.perform(put("/api/rooms/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> "manager@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));
    }

    @Test
    void deleteRoom_ShouldReturn204() throws Exception {
        doNothing().when(roomService).deleteRoom(10L, "manager@test.com");

        mockMvc.perform(delete("/api/rooms/10")
                .principal(() -> "manager@test.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void setRoomAvailability_ShouldReturn200() throws Exception {
        doNothing().when(roomService).setRoomAvailability(10L, false, "manager@test.com");

        mockMvc.perform(patch("/api/rooms/10/availability")
                .param("isAvailable", "false")
                .principal(() -> "manager@test.com"))
                .andExpect(status().isOk());
    }
}

