package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.PageResponse;
import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.elasticsearch.HotelElasticRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelSearchServiceImplTest {

    @Mock
    private HotelElasticRepository hotelElasticRepository;

    @InjectMocks
    private HotelSearchServiceImpl hotelSearchService;

    private HotelDocument mockHotelDocument;
    private Page<HotelDocument> mockPage;

    @BeforeEach
    void setUp() {
        mockHotelDocument = HotelDocument.builder()
                .id("1")
                .name("Test Hotel")
                .city("Istanbul")
                .district("Kadikoy")
                .description("Great view")
                .rating(4.5)
                .build();

        mockPage = new PageImpl<>(List.of(mockHotelDocument), PageRequest.of(0, 10), 1);
    }

    @Test
    void searchInElasticsearch_WhenNoSearchText_ShouldReturnAll() {
        // Arrange
        HotelSearchRequest request = new HotelSearchRequest(null, null, null, null, null, null, null, null, null);
        when(hotelElasticRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        Page<HotelDocument> result = hotelSearchService.searchInElasticsearch(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Hotel", result.getContent().get(0).getName());
        verify(hotelElasticRepository, times(1)).findAll(any(Pageable.class));
        verify(hotelElasticRepository, never()).searchAcrossAllFields(anyString(), any(Pageable.class));
    }

    @Test
    void searchInElasticsearch_WhenSearchTextProvided_ShouldSearchAcrossFields() {
        // Arrange
        HotelSearchRequest request = new HotelSearchRequest("Istanbul", null, null, null, null, null, null, 0, 10);
        when(hotelElasticRepository.searchAcrossAllFields(anyString(), any(Pageable.class))).thenReturn(mockPage);

        // Act
        Page<HotelDocument> result = hotelSearchService.searchInElasticsearch(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(hotelElasticRepository, times(1)).searchAcrossAllFields(eq("Istanbul"), any(Pageable.class));
        verify(hotelElasticRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void searchInElasticsearch_WhenMultipleFieldsProvided_ShouldConcatenateSearchText() {
        // Arrange
        HotelSearchRequest request = new HotelSearchRequest("Istanbul", "Great", "Kadikoy", null, null, null, null, 0, 10);
        when(hotelElasticRepository.searchAcrossAllFields(anyString(), any(Pageable.class))).thenReturn(mockPage);

        // Act
        hotelSearchService.searchInElasticsearch(request);

        // Assert
        verify(hotelElasticRepository, times(1)).searchAcrossAllFields(eq("Istanbul Kadikoy Great"), any(Pageable.class));
    }

    @Test
    void getTopRatedHotels_ShouldReturnSortedByRating() {
        // Arrange
        int size = 5;
        when(hotelElasticRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        // Act
        PageResponse<HotelDocument> result = hotelSearchService.getTopRatedHotels(size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
        
        // Verify that Pageable was created with Sort.Direction.DESC and "rating"
        verify(hotelElasticRepository).findAll(argThat((Pageable pageable) -> {
            Sort.Order order = pageable.getSort().getOrderFor("rating");
            return order != null && order.getDirection() == Sort.Direction.DESC && pageable.getPageSize() == size;
        }));
    }
}
