package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.service.HotelSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Hotel Search Controller", description = "Elasticsearch tabanlı gelişmiş otel arama işlemleri")
public class HotelSearchController {

    private final HotelSearchService hotelSearchService;

    @PostMapping("/hotels")
    public ResponseEntity<Page<HotelDocument>> searchHotels(@Valid @RequestBody HotelSearchRequest request) {
        return ResponseEntity.ok(hotelSearchService.searchInElasticsearch(request));
    }

    //En iyi otelleri listeleyen metot
    @GetMapping("/top-rated")
    public ResponseEntity<Page<HotelDocument>> getTopRatedHotels(
            @RequestParam(defaultValue = "10") int size //kullanıcı değer girmezse 10 adet yani default sırala demek
    ) {
        return ResponseEntity.ok(hotelSearchService.getTopRatedHotels(size));
    }
}