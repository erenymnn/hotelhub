package com.example.hotelhub.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest (

        @NotNull(message = "Oda ID boş olamaz!")
        Long roomId,
//kötü niyetli biri Postmanden userId: 5 yazıp başkasının adına rezervasyon yapabilirdi. Biz, işlemi yapan kişinin kim olduğunu dışarıdan gelen veriye güvenerek değil, JWT Token'ından (kimlik kartından) okuyarak arka planda kendimiz bulacağız.
        @NotNull(message = "Giriş tarihi boş olamaz!")
        @FutureOrPresent(message = "Giriş tarihi bugün veya gelecekte olmalıdır!")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate checkInDate,

        @NotNull(message = "Çıkış tarihi boş olamaz!")
        @Future(message = "Çıkış tarihi kesinlikle gelecekte bir tarih olmalıdır!")
        @JsonFormat(pattern = "yyyy-MM-dd") //boş gelebilecegini de öğretiyoruz
        LocalDate checkOutDate

){
}
