package com.example.hotelhub.controller;

import com.example.hotelhub.service.ElasticsearchIndexService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ElasticsearchAdminController.class,
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
@AutoConfigureMockMvc(addFilters = false) // Güvenlik katmanını testte devre dışı bırakıyoruz ki sadece controller'ı test edelim
class ElasticsearchAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElasticsearchIndexService elasticsearchIndexService;

    @Test
    void recreateIndexAndSwapAlias_ShouldReturn200AndMessage() throws Exception {
        String successMessage = "Sıfır Kesinti Takas Başarılı! Yeni Index: hotel_index_123456";
        when(elasticsearchIndexService.recreateIndexAndSwapAlias()).thenReturn(successMessage);

        mockMvc.perform(post("/api/admin/elasticsearch/recreate-index")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }

    @Test
    void bulkSyncHotels_ShouldReturn200AndMessage() throws Exception {
        String successMessage = "Bulk Sync Başarılı! Toplam 500 otel Elasticsearch'e aktarıldı.";
        when(elasticsearchIndexService.bulkSyncHotels()).thenReturn(successMessage);

        mockMvc.perform(post("/api/admin/elasticsearch/bulk-sync")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }
}

