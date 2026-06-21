package com.example.hotelhub.event;

import java.io.Serializable;

public record HotelSyncEvent(
        Long id,
        String name,
        String description,
        String city,
        String district,
        Double rating
) implements Serializable {
    //sistem geriye dönük uyumlu, ileride event nesnesine yeni bir alan eklediğimizde, RabbitMQ kuyruğunda bekleyen eski mesajlar InvalidClassException hatasıyla patlamasın, sistem geriye dönük uyumlu çalışsın diye koydum.
    private static final long serialVersionUID = 1L;
}






