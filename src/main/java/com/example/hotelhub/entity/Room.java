package com.example.hotelhub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    // Para birimi hassasiyeti eklendi
    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    private Integer capacity;

    // Varsayılan olarak true atadık
    private Boolean isAvailable=true;

    // Performans için LAZY eklendi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
