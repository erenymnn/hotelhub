package com.example.hotelhub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HotelRequest(
        //(Kullanıcıdan Gelen İstek) kısmıdır
        //stringlerde notblank kullandık digerlerinde notnull

        @NotBlank(message = "Otel adı boş bırakılamaz!")
        String name,

        @NotBlank(message = "Şehir boş bırakılamaz!")
        String city,

        @NotBlank(message = "Adres boş bırakılamaz!")
        String address,

        @NotNull(message = "Otel puanı boş bırakılamaz!")
        @Min(value = 1,message = "Otel puanı en az 1 olabilir")
        @Max(value = 5,message = "Otel puanı en fazla 5 olabilir")
        Double rating  //rating otelin yıldız seviyesi
) {
}
