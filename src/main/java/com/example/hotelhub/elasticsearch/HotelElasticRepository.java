package com.example.hotelhub.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface HotelElasticRepository extends ElasticsearchRepository<HotelDocument,String> {

    // Otel adına veya açıklamasına göre akıllı metin araması (Full-Text Search)
    List<HotelDocument> findByNameContainingOrDescriptionContaining(String name, String description);

    // Şehre göre doğrudan filtreleme
    List<HotelDocument> findByCity(String city);
}
