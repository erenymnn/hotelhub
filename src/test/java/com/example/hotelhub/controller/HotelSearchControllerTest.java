package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.PageResponse;
import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.service.HotelSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = HotelSearchController.class,
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
class HotelSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HotelSearchService hotelSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    private HotelDocument hotelDocument;

    @BeforeEach
    void setUp() {
        hotelDocument = HotelDocument.builder()
                .id("1")
                .name("Test Hotel")
                .city("Istanbul")
                .rating(4.5)
                .build();
    }

    @Test
    void searchHotels_ShouldReturn200AndResults() throws Exception {
        HotelSearchRequest request = new HotelSearchRequest("Istanbul", null, null, null, null, null, null, 0, 10);
        Page<HotelDocument> page = new PageImpl<>(List.of(hotelDocument));
        
        when(hotelSearchService.searchInElasticsearch(any(HotelSearchRequest.class))).thenReturn(page);

        mockMvc.perform(post("/api/v1/search/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Hotel"));
    }

    @Test
    void getTopRatedHotels_ShouldReturn200() throws Exception {
        Page<HotelDocument> page = new PageImpl<>(List.of(hotelDocument));
        PageResponse<HotelDocument> pageResponse = PageResponse.of(page, List.of(hotelDocument));
        
        when(hotelSearchService.getTopRatedHotels(eq(5))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/search/top-rated")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Hotel"));
    }
}

