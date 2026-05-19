package com.example.hotelhub.entity;

public enum BookingStatus {
    PENDING, //Beklemede Kullanıcı "Odayı istiyorum" dedi ama henüz kredi kartından para çekilmedi veya yönetici onaylamadı
    CONFIRMED, //Onaylandı
    CANCELED
}
