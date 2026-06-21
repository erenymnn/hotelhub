package com.example.hotelhub.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    // BOOKING NOTIFICATION CONSTANTS
    public static final String BOOKING_QUEUE = "booking.notification.queue";
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String BOOKING_ROUTING_KEY = "booking.routing.key";

    // ELASTICSEARCH HOTEL SYNC CONSTANTS
    public static final String HOTEL_SYNC_QUEUE = "hotel.sync.queue";
    public static final String HOTEL_EXCHANGE = "hotel.exchange";
    public static final String HOTEL_SYNC_ROUTING_KEY = "hotel.sync.routing.key";


    // BOOKING BEANS
    @Bean
    public Queue bookingQueue() { // İsmini queue yerine bookingQueue yaptık ki karışmasın
        return new Queue(BOOKING_QUEUE, true);
    }

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    //  Parametre isimlerini tam üreten metot adlarıyla eşledik (Spring çakışmasın diye)
    public Binding bookingBinding(Queue bookingQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingQueue).to(bookingExchange).with(BOOKING_ROUTING_KEY);
    }


    // HOTEL SYNC BEANS

    @Bean
    public Queue hotelSyncQueue() {
        return QueueBuilder.durable(HOTEL_SYNC_QUEUE).build();
    }

    @Bean
    public DirectExchange hotelExchange() {
        return new DirectExchange(HOTEL_EXCHANGE);
    }

    @Bean
    public Binding hotelSyncBinding(Queue hotelSyncQueue, DirectExchange hotelExchange) {
        return BindingBuilder.bind(hotelSyncQueue).to(hotelExchange).with(HOTEL_SYNC_ROUTING_KEY);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new ContentTypeDelegatingMessageConverter();
    }
}