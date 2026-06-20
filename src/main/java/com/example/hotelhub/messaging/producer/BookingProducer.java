package com.example.hotelhub.messaging.producer;

import com.example.hotelhub.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingProducer.class);

    // Spring'in bize sağladığı resmi mesaj gönderme şablonu
    private final RabbitTemplate rabbitTemplate;

    public BookingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendBookingNotification(String message) {
        log.info("Sending message to RabbitMQ exchange: -> {}", message);

        // Mesajı Exchange'e, belirlediğimiz Routing Key ile fırlatıyoruz
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKING_EXCHANGE,
                RabbitMQConfig.BOOKING_ROUTING_KEY,
                message
        );
    }
}