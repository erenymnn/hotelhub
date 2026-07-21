package com.example.hotelhub.service;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.PageResponse;
import com.example.hotelhub.elasticsearch.HotelDocument;
import org.springframework.data.domain.Page;



public interface HotelSearchService {
    Page<HotelDocument> searchInElasticsearch(HotelSearchRequest request);
    PageResponse<HotelDocument> getTopRatedHotels(int size); //
}
