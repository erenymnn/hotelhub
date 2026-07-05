package com.example.hotelhub.messaging.consumer;

import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.elasticsearch.HotelElasticRepository;
import com.example.hotelhub.event.HotelDeleteEvent;
import com.example.hotelhub.event.HotelSyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelSyncConsumer {

    private final HotelElasticRepository hotelElasticRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) //eğer veritabanında başarıyla bir işlem olursa elasticSearch güncelle diyoruz.
    public void consumeHotelSyncEvent(HotelSyncEvent event) {
        log.info("Spring Event Bus'tan yeni otel senkronizasyon eventi alındı! Otel ID: {}", event.id());

        try {
            HotelDocument document = HotelDocument.builder()
                    .id(event.id().toString())
                    .name(event.name())
                    .description(event.description())
                    .city(event.city())
                    .district(event.district())
                    .rating(event.rating())
                    .build();

            hotelElasticRepository.save(document);

            log.info("Otel başarıyla Elasticsearch'e yazıldı! ID: {}", document.getId());
        } catch (Exception e) {
            log.error("Elasticsearch'e yazarken hata fırlatıldı.", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void consumeHotelDeleteEvent(HotelDeleteEvent event) {
        log.info("Silme eventi alındı. Elasticsearch'ten siliniyor! Otel ID: {}", event.hotelId());
        try {
            hotelElasticRepository.deleteById(event.hotelId().toString());
            log.info("Otel Elasticsearch'ten başarıyla silindi! ID: {}", event.hotelId());
        } catch (Exception e) {
            log.error("Elasticsearch'ten silinirken hata oluştu.", e);
        }
    }
}
