package com.example.hotelhub.messaging.producer;

import com.example.hotelhub.config.RabbitMQConfig;
import com.example.hotelhub.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingProducer {


    private final RabbitTemplate rabbitTemplate;

    // String değil, BookingEvent nesnesi alıyoruz
    public void sendBookingNotification(BookingEvent event) {
        log.info("Sending booking event to RabbitMQ: -> ID: {}", event.bookingId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_EXCHANGE,
                RabbitMQConfig.BOOKING_ROUTING_KEY,
                event // Nesneyi gönderiyoruz, RabbitTemplate bunu JSON yapacak
        );
    }
}