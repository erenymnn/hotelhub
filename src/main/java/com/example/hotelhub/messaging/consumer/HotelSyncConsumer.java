package com.example.hotelhub.messaging.consumer;

import com.example.hotelhub.event.HotelSyncEvent;
import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.elasticsearch.HotelElasticRepository; // 🔥 Senin reponu bağladık
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelSyncConsumer {


    private final HotelElasticRepository hotelElasticRepository;

    @Async // Ana thread'i bloklamadan arka planda asenkron çalıştırır
    @EventListener // Spring Event Bus'a düşen HotelSyncEvent'leri otomatik yakalar
    public void consumeHotelSyncEvent(HotelSyncEvent event) {
        log.info(" Spring Event Bus'tan yeni otel senkronizasyon eventi alındı! Otel ID: {}", event.id());

        try {
            // Alanların hepsi eksiksiz map ediliyor
            HotelDocument document = HotelDocument.builder()
                    .id(event.id().toString())
                    .name(event.name())
                    .description(event.description())
                    .city(event.city())
                    .district(event.district())
                    .rating(event.rating())
                    .build();

            hotelElasticRepository.save(document);

            log.info(" [Consumer] Otel başarıyla Elasticsearch'e yazıldı! ID: {}", document.getId());

        } catch (Exception e) {
            log.error(" [Consumer] Elasticsearch'e yazarken hata fırlatıldı! Hata detayı: ", e);
        }
    }
}