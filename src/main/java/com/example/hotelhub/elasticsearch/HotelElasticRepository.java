package com.example.hotelhub.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface HotelElasticRepository extends ElasticsearchRepository<HotelDocument, String> {

    // Modern Arama: İsmi, Şehri, İlçeyi ve Açıklamayı aynı anda, ağırlıklandırarak (boosting) arar.
    @Query("{" +
            "  \"multi_match\": {" +
            "    \"query\": \"?0\"," +
            "    \"fields\": [\"name^3\", \"city^2\", \"district^2\", \"description\"]," +
            "    \"fuzziness\": \"AUTO\"" +
            "  }" +
            "}")
    Page<HotelDocument> searchAcrossAllFields(String query, Pageable pageable);

    // En yüksek puanlıları getirmek için özel bir sorguya ihtiyacın yok,
    // Spring Data'nın Pageable'ına Sort ekleyerek bunu serviste halledeceğiz.
}
