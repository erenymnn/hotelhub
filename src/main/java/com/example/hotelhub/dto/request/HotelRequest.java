package com.example.hotelhub.dto.request;

import jakarta.validation.constraints.*;

public record HotelRequest(
        //(Kullanıcıdan Gelen İstek) kısmıdır
        //stringlerde notblank kullandık digerlerinde notnull

        @NotBlank(message = "Otel adı boş bırakılamaz!")
        String name,

        @NotBlank(message = "Şehir boş bırakılamaz!")
        String city,
        @NotBlank(message = "İlçe boş bırakılamaz!")
        String district,

        @NotBlank(message = "Adres boş bırakılamaz!")
        String address,

        @NotBlank(message = "Numara boş bırakılamaz!")
        @Pattern(regexp = "^[0-9]{11}$", message = "Telefon numarası sadece 11 haneli rakamlardan oluşmalıdır!")
        String number,

        @NotBlank(message = "E-posta alanı boş bırakılamaz")
        @Email(message = "Lütfen geçerli bir e-posta adresi formatı giriniz (Örn: isim@domain.com)")
        String email,

        @NotBlank(message = "Açıklama boş bırakılamaz!")
        String description,

        @NotNull(message = "Otel puanı boş bırakılamaz!")
        @Min(value = 1, message = "Otel puanı en az 1 olabilir!")
        @Max(value = 5, message = "Otel puanı en fazla 5 olabilir!")
        Double rating  //rating otelin yıldız seviyesi
) {
}
