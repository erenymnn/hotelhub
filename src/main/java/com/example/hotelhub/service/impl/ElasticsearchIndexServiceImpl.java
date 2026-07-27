package com.example.hotelhub.service.impl;

import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import com.example.hotelhub.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.hotelhub.service.ElasticsearchIndexService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexServiceImpl implements ElasticsearchIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final HotelRepository hotelRepository;

    @Lazy
    @Autowired
    private ElasticsearchIndexServiceImpl self;

    private static final String ALIAS_NAME = "hotel_alias";

    public String recreateIndexAndSwapAlias() {

        String newIndexName = "hotel_index_" + System.currentTimeMillis();
        log.info("Yeni index oluşturuluyor: {}", newIndexName);

        IndexOperations classIndexOps = elasticsearchOperations.indexOps(HotelDocument.class);
        IndexOperations newIndexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(newIndexName));


        newIndexOps.create(classIndexOps.createSettings(), classIndexOps.createMapping());


        AliasActions aliasActions = new AliasActions();
        Set<String> indicesToDelete = new HashSet<>();
        
        try {
            IndexOperations aliasOps = elasticsearchOperations.indexOps(IndexCoordinates.of(ALIAS_NAME));
            Map<String, Set<org.springframework.data.elasticsearch.core.index.AliasData>> aliases = aliasOps.getAliases(ALIAS_NAME);

            // Eski indexleri alias'tan çıkar
            if (aliases != null && !aliases.isEmpty()) {
                for (String oldIndexName : aliases.keySet()) {
                    log.info("Alias'tan çıkarılacak eski index: {}", oldIndexName);
                    aliasActions.add(new AliasAction.Remove(AliasActionParameters.builder()
                            .withIndices(oldIndexName)
                            .withAliases(ALIAS_NAME)
                            .build()));
                    
                    // Silinecek indexleri listeye ekle
                    indicesToDelete.add(oldIndexName);
                }
            }
        } catch (Exception e) {
            log.warn("Alias kontrolü sırasında bir hata oluştu veya alias henüz yok. İlk kurulum olabilir: {}", e.getMessage());
        }

        // 4. Alias'ı yeni oluşturduğumuz index'e bağla
        log.info("Alias '{}' yeni index'e bağlandı: {}", ALIAS_NAME, newIndexName);
        aliasActions.add(new AliasAction.Add(AliasActionParameters.builder()
                .withIndices(newIndexName)
                .withAliases(ALIAS_NAME)
                .build()));


        newIndexOps.alias(aliasActions);

        //
        for (String oldIndexName : indicesToDelete) {
            log.info("Eski index fiziksel olarak siliniyor: {}", oldIndexName);
            elasticsearchOperations.indexOps(IndexCoordinates.of(oldIndexName)).delete();
        }

        return "Sıfır Kesinti Takas Başarılı! Yeni Index: " + newIndexName;
    }

    @Override
    public String bulkSyncHotels() {
        log.info("Bulk Sync işlemi başladı...");
        int pageSize = 500;
        int pageNumber = 0;
        int totalSynced = 0;
        Page<Hotel> page;

        do {
            page = hotelRepository.findAll(PageRequest.of(pageNumber, pageSize));
            if (!page.hasContent()) {
                break;
            }

            List<HotelDocument> documents = page.getContent().stream()
                    .map(hotel -> HotelDocument.builder()
                            .id(hotel.getId().toString())
                            .name(hotel.getName())
                            .description(hotel.getDescription())
                            .city(hotel.getCity())
                            .district(hotel.getDistrict())
                            .rating(hotel.getRating())
                            .build())
                    .toList();

            self.saveAllWithRetry(documents);
            totalSynced += documents.size();
            log.info("Page {} başarıyla Elasticsearch'e gönderildi. Toplam aktarılan: {}", pageNumber, totalSynced);

            pageNumber++;
        } while (page.hasNext());

        return "Bulk Sync Başarılı! Toplam " + totalSynced + " otel Elasticsearch'e aktarıldı.";
    }

    @Retryable(
            value = Exception.class,
            maxAttempts = 3, 
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void saveAllWithRetry(List<HotelDocument> documents) {
        log.info("Elasticsearch'e {} adet kayıt Bulk API ile gönderiliyor...", documents.size());
        elasticsearchOperations.save(documents, IndexCoordinates.of(ALIAS_NAME));
    }
}
