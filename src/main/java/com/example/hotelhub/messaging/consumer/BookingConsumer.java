package com.example.hotelhub.messaging.consumer;

import com.example.hotelhub.config.RabbitMQConfig;
import com.example.hotelhub.event.BookingEvent;
import com.example.hotelhub.event.HotelSyncEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class BookingConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingConsumer.class);

    // @RabbitListener sihirli anotasyondur. Belirtilen kuyruğu 7/24 asenkron olarak dinler.
    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void consumeBookingMessage(BookingEvent bookingEvent) {
        log.info("==> REZERVASYON MESAJI ALINDI: ID: {}", bookingEvent.bookingId());

        // Reelde burada MailSender kütüphaneleri çağrılır ve müşteriye mail basılır.
        log.info("Processing background task (e.g., Sending confirmation email to customer)...");
    }
}