package com.example.hotelhub.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@SQLDelete(sql = "UPDATE hotels SET is_Deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String city;
    private String district; //sadece şehir degil ilçe bilgiside gerekli
    private String address;
    private Double rating;
    private String number; //başındaki sıfırların kaybolmaması gereken veriler her zaman String olarak tutulmalıdır
    private String email;
    @Column(columnDefinition = "TEXT") //bu anatasyon @Lob a göre daha temiz ve güvenli yoldur . @Lob veriyi başka bir tabloda tutup ıd ile referans verme yapabilir.
    private String description;
    private boolean is_Deleted=false;
    @OneToMany(mappedBy = "hotel",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Room> rooms=new ArrayList<>();

}
