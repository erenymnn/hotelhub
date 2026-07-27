package com.example.hotelhub.service.impl;

import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElasticsearchIndexServiceImplTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private IndexOperations indexOperations;

    @InjectMocks
    private ElasticsearchIndexServiceImpl elasticsearchIndexService;

    @BeforeEach
    void setUp() {
        // Mock self reference to avoid NullPointerException on self.saveAllWithRetry
        org.springframework.test.util.ReflectionTestUtils.setField(elasticsearchIndexService, "self", elasticsearchIndexService);
    }

    @Test
    void recreateIndexAndSwapAlias_ShouldCreateNewIndexAndSwap() {
        when(elasticsearchOperations.indexOps(any(Class.class))).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(any(IndexCoordinates.class))).thenReturn(indexOperations);
        
        when(indexOperations.createSettings()).thenReturn(mock(org.springframework.data.elasticsearch.core.index.Settings.class));
        when(indexOperations.createMapping()).thenReturn(mock(org.springframework.data.elasticsearch.core.document.Document.class));
        
        // Mocking getAliases returning empty to simulate first run
        when(indexOperations.getAliases(anyString())).thenReturn(Map.of());

        String result = elasticsearchIndexService.recreateIndexAndSwapAlias();

        assertTrue(result.contains("Sıfır Kesinti Takas Başarılı!"));
        verify(indexOperations, times(1)).create(any(), any());
        verify(indexOperations, times(1)).alias(any(AliasActions.class));
    }

    @Test
    void bulkSyncHotels_ShouldFetchFromDBAndSaveToES() {
        Hotel mockHotel = new Hotel();
        mockHotel.setId(1L);
        mockHotel.setName("Test Hotel");
        mockHotel.setCity("Test City");
        
        Page<Hotel> mockPage = new PageImpl<>(List.of(mockHotel), PageRequest.of(0, 500), 1);
        Page<Hotel> emptyPage = Page.empty();

        when(hotelRepository.findAll(any(PageRequest.class)))
                .thenReturn(mockPage);

        String result = elasticsearchIndexService.bulkSyncHotels();

        assertTrue(result.contains("Toplam 1 otel"));
        verify(hotelRepository, times(1)).findAll(any(PageRequest.class));
        verify(elasticsearchOperations, times(1)).save(anyList(), any(IndexCoordinates.class));
    }
}
