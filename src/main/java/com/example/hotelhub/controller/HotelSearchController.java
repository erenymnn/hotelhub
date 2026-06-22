package com.example.hotelhub.controller;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.service.HotelSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class HotelSearchController {

    private final HotelSearchService hotelSearchService;

    @PostMapping("/hotels")
    public ResponseEntity<Page<HotelDocument>> searchHotels(@RequestBody HotelSearchRequest request) {
        return ResponseEntity.ok(hotelSearchService.searchInElasticsearch(request));
    }
}