package com.example.hotelhub.entity;

import com.example.hotelhub.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@SQLDelete(sql = "UPDATE rooms SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomNumber;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    private RoomType type;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    private Integer capacity;

    private Boolean isAvailable = true;
    private Boolean hasAirConditioning = true;
    private Boolean hasBalcony = true;

    private String viewType;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @ElementCollection
    @CollectionTable(name = "room_features", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;
}
