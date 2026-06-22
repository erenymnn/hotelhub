package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.elasticsearch.HotelElasticRepository;
import com.example.hotelhub.service.HotelSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotelSearchServiceImpl implements HotelSearchService {

    private final HotelElasticRepository hotelElasticRepository;

    @Override
    public Page<HotelDocument> searchInElasticsearch(HotelSearchRequest request) {

        int pageNumber = (request.page() != null) ? request.page() : 0;
        int pageSize = (request.size() != null) ? request.size() : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);


        if (request.city() != null && !request.city().isEmpty()) {
            return hotelElasticRepository.findByCityMatches(request.city(), pageable);
        }


        if (request.district() != null && !request.district().isEmpty()) {
            return hotelElasticRepository.findByDistrictMatches(request.district(), pageable);
        }


        if (request.description() != null && !request.description().isEmpty()) {
            return hotelElasticRepository.findByNameMatchesOrDescriptionMatches(
                    request.description(),
                    request.description(),
                    pageable
            );
        }


        return hotelElasticRepository.findAll(pageable);
    }
}