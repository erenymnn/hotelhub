package com.example.hotelhub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
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
    private String address;
    private Double rating;
    @OneToMany(mappedBy = "hotel",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Room> rooms=new ArrayList<>();

}
