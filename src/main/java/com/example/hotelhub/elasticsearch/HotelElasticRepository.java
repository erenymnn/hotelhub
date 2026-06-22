package com.example.hotelhub.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelElasticRepository extends ElasticsearchRepository<HotelDocument, String> {


    Page<HotelDocument> findByCityMatches(String city, Pageable pageable);


    Page<HotelDocument> findByDistrictMatches(String district, Pageable pageable);


    Page<HotelDocument> findByNameMatchesOrDescriptionMatches(String name, String description, Pageable pageable);

}