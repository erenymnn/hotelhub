package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.enums.BookingStatus;
import com.example.hotelhub.service.BookingService;
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
import java.time.LocalDate;
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
    controllers = BookingController.class,
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
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        bookingResponse = new BookingResponse(1L, "CODE123", 1L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), BigDecimal.valueOf(200.0), BookingStatus.PENDING);
    }

    @Test
    void createBooking_ShouldReturn201() throws Exception {
        BookingRequest request = new BookingRequest(1L, LocalDate.now(), LocalDate.now().plusDays(2));
        when(bookingService.createBooking(any(BookingRequest.class), anyString())).thenReturn(bookingResponse);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(() -> "user@test.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserBookings_ShouldReturn200AndList() throws Exception {
        when(bookingService.getUserBookings(anyString())).thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/api/bookings/my-bookings")
                .principal(() -> "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void cancelBooking_ShouldReturn204() throws Exception {
        doNothing().when(bookingService).cancelBooking(1L, "user@test.com");

        mockMvc.perform(delete("/api/bookings/1")
                .principal(() -> "user@test.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateBookingStatus_ShouldReturn200() throws Exception {
        doNothing().when(bookingService).updateBookingStatus(eq(1L), any(BookingStatus.class));

        mockMvc.perform(patch("/api/bookings/1/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk());
    }
}

