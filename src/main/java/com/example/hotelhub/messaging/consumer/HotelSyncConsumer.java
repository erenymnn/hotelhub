package com.example.hotelhub.messaging.consumer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.hotelhub.event.HotelSyncEvent;
import com.example.hotelhub.elasticsearch.HotelDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelSyncConsumer {

    private final ElasticsearchClient elasticsearchClient;

    @Async
    @EventListener
    public void consumeHotelSyncEvent(HotelSyncEvent event) {
        log.info(" Spring Event Bus'tan yeni otel senkronizasyon eventi alındı! Otel ID: {}", event.id());

        try {
            HotelDocument document = HotelDocument.builder()
                    .id(event.id().toString())
                    .name(event.name())
                    .description(event.description())
                    .city(event.city())
                    .district(event.district())
                    .rating(event.rating())
                    .build();

            //  Doğrudan istemci üzerinden Elasticsearch "hotel_index" indeksine veriyi asenkron yazıyoruz
            elasticsearchClient.index(i -> i
                    .index("hotel_index")
                    .id(document.getId())
                    .document(document)
            );
            log.info(" Otel başarıyla Elasticsearch üzerinde indekslendi. ID: {}", document.getId());

        } catch (Exception e) {
            log.error(" Elasticsearch senkronizasyonu sırasında hata oluştu!", e);
        }
    }
}