package com.example.hotelhub.service.impl;

import com.example.hotelhub.elasticsearch.HotelDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import com.example.hotelhub.service.ElasticsearchIndexService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexServiceImpl implements ElasticsearchIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private static final String ALIAS_NAME = "hotel_alias";

    public String recreateIndexAndSwapAlias() {

        String newIndexName = "hotel_index_" + System.currentTimeMillis();
        log.info("Yeni index oluşturuluyor: {}", newIndexName);

        IndexOperations classIndexOps = elasticsearchOperations.indexOps(HotelDocument.class);
        IndexOperations newIndexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(newIndexName));


        newIndexOps.create(classIndexOps.createSettings(), classIndexOps.createMapping());


        AliasActions aliasActions = new AliasActions();
        java.util.Set<String> indicesToDelete = new java.util.HashSet<>();
        
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
}
