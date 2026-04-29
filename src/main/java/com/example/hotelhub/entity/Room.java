package com.example.hotelhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@SQLDelete(sql = "UPDATE rooms SET is_Deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter @Setter
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

    private boolean is_Deleted = false; // Soft delete için bu alan şart
    @ElementCollection
    @CollectionTable(name = "room_features", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    // Performans için LAZY eklendi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
