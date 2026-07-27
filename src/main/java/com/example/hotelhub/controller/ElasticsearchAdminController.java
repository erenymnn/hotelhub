package com.example.hotelhub.controller;

import com.example.hotelhub.service.ElasticsearchIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/elasticsearch")
@RequiredArgsConstructor
@Tag(name = "Elasticsearch Admin API", description = "Sıfır Kesinti ile Index takası (Zero Downtime) gibi yönetimsel işlemler")
public class ElasticsearchAdminController {

    private final ElasticsearchIndexService elasticsearchIndexService;

    @PostMapping("/recreate-index")
    @Operation(summary = "Sıfır Kesinti Index Takası (Zero Downtime Alias Swap)", description = "Yeni bir index oluşturur, mappingleri uygular ve alias'ı otomatik olarak yeni indexe geçirir.")
    public ResponseEntity<String> recreateIndex() {
        String result = elasticsearchIndexService.recreateIndexAndSwapAlias();
        return ResponseEntity.ok(result);
    }
}
