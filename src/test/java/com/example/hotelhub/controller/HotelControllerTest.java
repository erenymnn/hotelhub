package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.dto.response.PageResponse;
import com.example.hotelhub.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = HotelController.class,
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
@AutoConfigureMockMvc(addFilters = false) // Disabling security filters for controller logic testing
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelService hotelService;

    @Autowired
    private ObjectMapper objectMapper;

    private HotelResponse hotelResponse;
    private PageResponse<HotelResponse> pageResponse;

    @BeforeEach
    void setUp() {
        hotelResponse = new HotelResponse(1L, "Test Hotel", "Test Desc", "Address", "City", "District", "1234", "test@hotel.com", 5.0, java.util.Collections.emptyList());
        Page<HotelResponse> page = new PageImpl<>(List.of(hotelResponse));
        pageResponse = PageResponse.of(page, List.of(hotelResponse));
    }

    @Test
    void getAllHotels_ShouldReturn200AndPageResponse() throws Exception {
        when(hotelService.getAllHotels(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Hotel"));
    }

    @Test
    void getHotelById_WhenExists_ShouldReturn200() throws Exception {
        when(hotelService.getHotelById(1L)).thenReturn(hotelResponse);

        mockMvc.perform(get("/api/hotels/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void create_ShouldReturn201AndCreatedHotel() throws Exception {
        HotelRequest request = new HotelRequest("Test Hotel", "Istanbul", "Besiktas", "Some Address", "05321234567", "test@hotel.com", "Desc", 5.0);
        when(hotelService.createHotel(any(HotelRequest.class), anyString())).thenReturn(hotelResponse);

        mockMvc.perform(post("/api/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> "manager@test.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void updateHotel_ShouldReturn200AndUpdatedHotel() throws Exception {
        HotelRequest request = new HotelRequest("Updated Hotel", "Istanbul", "Besiktas", "Some Address", "05321234567", "test@hotel.com", "Desc", 5.0);
        when(hotelService.updateHotel(eq(1L), any(HotelRequest.class), anyString())).thenReturn(hotelResponse);

        mockMvc.perform(put("/api/hotels/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> "manager@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void deleteHotel_ShouldReturn204() throws Exception {
        doNothing().when(hotelService).deleteHotel(1L, "manager@test.com");

        mockMvc.perform(delete("/api/hotels/1")
                .principal(() -> "manager@test.com"))
                .andExpect(status().isNoContent());
    }
}

