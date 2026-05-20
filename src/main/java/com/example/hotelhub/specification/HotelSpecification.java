package com.example.hotelhub.specification;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.entity.Hotel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class HotelSpecification {

    public static Specification<Hotel> filterHotels(HotelSearchRequest request) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 1. Şehir Filtresi (En Güvenli UPPER Eşleşmesi)
            if (request.city() != null && !request.city().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.function("UPPER", String.class, root.get("city")),
                        "%" + request.city().toUpperCase(java.util.Locale.forLanguageTag("tr-TR")) + "%"
                ));
            }

            // 2. İlçe Filtresi
            if (request.district() != null && !request.district().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.function("UPPER", String.class, root.get("district")),
                        "%" + request.district().toUpperCase(java.util.Locale.forLanguageTag("tr-TR")) + "%"
                ));
            }

            // 3. Minimum Puan Filtresi
            if (request.minRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("rating"),
                        request.minRating()
                ));
            }
// 4. MAKSİMUM FİYAT FİLTRESİ (Otel -> Oda İlişkisi üzerinden Join)
            if (request.maxPrice() != null) {
                // Hotel sınıfındaki liste adı: "rooms"
                jakarta.persistence.criteria.Join<Object, Object> roomsJoin = root.join("rooms");

                // Room sınıfındaki fiyat değişkeni adı: "pricePerNight"
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        roomsJoin.get("pricePerNight"),
                        request.maxPrice()
                ));

                // Optimizasyon
                query.distinct(true);
            }
            // Silinmemiş otelleri getir
            predicates.add(criteriaBuilder.isFalse(root.get("is_Deleted")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}