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

            // Şehir Filtresi (En Güvenli UPPER Eşleşmesi)
            if (request.city() != null && !request.city().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.function("UPPER", String.class, root.get("city")),
                        "%" + request.city().toUpperCase(java.util.Locale.forLanguageTag("tr-TR")) + "%"
                ));
            }

            //  İlçe Filtresi
            if (request.district() != null && !request.district().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.function("UPPER", String.class, root.get("district")),
                        "%" + request.district().toUpperCase(java.util.Locale.forLanguageTag("tr-TR")) + "%"
                ));
            }

            //  Minimum Puan Filtresi
            if (request.minRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("rating"),
                        request.minRating()
                ));
            }
         //  MAKSİMUM FİYAT FİLTRESİ (Otel -> Oda İlişkisi üzerinden Join)
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

            // MÜSAİTLİK VE TARİH FİLTRESİ
            if (request.checkInDate() != null && request.checkOutDate() != null) {

                //  Otele ait odalara bağlan (Sadece odası olan oteller gelsin)
                jakarta.persistence.criteria.Join<Object, Object> roomJoin = root.join("rooms");

                // Belirtilen tarihlerde DOLU olan odaların ID'lerini bul
                jakarta.persistence.criteria.Subquery<Long> busyRoomsQuery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<com.example.hotelhub.entity.Booking> bookingRoot = busyRoomsQuery.from(com.example.hotelhub.entity.Booking.class);

                // Alt sorgu bize sadece dolu odaların ID'sini döndürecek
                busyRoomsQuery.select(bookingRoot.get("room").get("id"));

                // (b.checkIn < reqOut) AND (b.checkOut > reqIn) AND (status = CONFIRMED)
                Predicate overlapCondition = criteriaBuilder.and(
                        criteriaBuilder.lessThan(bookingRoot.get("checkInDate"), request.checkOutDate()),
                        criteriaBuilder.greaterThan(bookingRoot.get("checkOutDate"), request.checkInDate()),
                        // Sadece ONAYLI rezervasyonlar odayı meşgul eder (Kendi Enum ismine göre CONFIRMED'i düzenleyebilirsin)
                        criteriaBuilder.equal(bookingRoot.get("status"), com.example.hotelhub.entity.BookingStatus.CONFIRMED)
                );
                busyRoomsQuery.where(overlapCondition);

                // 3. Ana Filtre: Otelin bu odasının ID'si, DOLU ODALAR (SubQuery) listesinin İÇİNDE OLMASIN (NOT IN)
                predicates.add(criteriaBuilder.not(roomJoin.get("id").in(busyRoomsQuery)));

                // Aynı oteli tekrarlı getirmemek için
                query.distinct(true);
            }


            // Silinmemiş otelleri getir
            predicates.add(criteriaBuilder.isFalse(root.get("is_Deleted")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }

}