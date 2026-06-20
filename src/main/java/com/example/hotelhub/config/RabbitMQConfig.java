package com.example.hotelhub.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Şirketlerde kuyruk, exchange ve routing key isimleri böyle sabit (final) olarak tanımlanır
    public static final String BOOKING_QUEUE = "booking.notification.queue";
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String BOOKING_ROUTING_KEY = "booking.routing.key";

    // Kuyruğu oluşturuyoruz (durable: true -> RabbitMQ çökse bile kuyruktaki mesajlar silinmez)
    @Bean
    public Queue queue() {
        return new Queue(BOOKING_QUEUE, true);
    }

    // 2. Mesaj dağıtıcısını (Exchange) oluşturuyoruz
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    // Kuyruk ile Dağıtıcıyı birbirine bağlıyoruz (Binding)
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(BOOKING_ROUTING_KEY);
    }
}