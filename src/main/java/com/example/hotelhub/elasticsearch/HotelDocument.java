package com.example.hotelhub.elasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "hotel_alias", createIndex = false)
public class HotelDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text, analyzer = "turkish")
    private String city;

    @Field(type = FieldType.Text, analyzer = "turkish")
    private String district;

    @Field(type = FieldType.Double)
    private Double rating;
}