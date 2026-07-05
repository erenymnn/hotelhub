package com.example.hotelhub.specification;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.entity.Booking;
import com.example.hotelhub.entity.enums.BookingStatus;
import com.example.hotelhub.entity.Hotel;
import com.example.hotelhub.entity.Room;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelSpecification {

    public static Specification<Hotel> filterHotels(HotelSearchRequest request) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();


            if (request.city() != null && !request.city().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.function("UPPER", String.class, root.get("city")),
                        "%" + request.city().toUpperCase(Locale.forLanguageTag("tr-TR")) + "%"
                ));
            }

            if (request.district() != null && !request.district().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.function("UPPER", String.class, root.get("district")),
                        "%" + request.district().toUpperCase(Locale.forLanguageTag("tr-TR")) + "%"
                ));
            }


            if (request.description() != null && !request.description().isBlank()) {
                String searchPattern = "%" + request.description().toUpperCase(Locale.forLanguageTag("tr-TR")) + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.function("UPPER", String.class, root.get("name")), searchPattern);
                Predicate descPredicate = criteriaBuilder.like(criteriaBuilder.function("UPPER", String.class, root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }


            if (request.minRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), request.minRating()));
            }


            Join<Hotel, Room> roomsJoin = null;
            if (request.maxPrice() != null || (request.checkInDate() != null && request.checkOutDate() != null)) {
                roomsJoin = root.join("rooms");
                query.distinct(true); // Aynı otelin mükerrer gelmesini engelle
            }


            if (request.maxPrice() != null && roomsJoin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(roomsJoin.get("pricePerNight"), request.maxPrice()));
            }


            if (request.checkInDate() != null && request.checkOutDate() != null && roomsJoin != null) {
                Subquery<Long> busyRoomsQuery = query.subquery(Long.class);
                Root<Booking> bookingRoot = busyRoomsQuery.from(Booking.class);

                busyRoomsQuery.select(bookingRoot.get("room").get("id"))
                        .where(criteriaBuilder.and(
                                criteriaBuilder.lessThan(bookingRoot.get("checkInDate"), request.checkOutDate()),
                                criteriaBuilder.greaterThan(bookingRoot.get("checkOutDate"), request.checkInDate()),
                                criteriaBuilder.equal(bookingRoot.get("status"), BookingStatus.CONFIRMED)
                        ));

                predicates.add(criteriaBuilder.not(roomsJoin.get("id").in(busyRoomsQuery))); // o tarihlerde dolu olan odaları cıkar.dolayısıyla o oteli cıkar.
            }


            predicates.add(criteriaBuilder.isFalse(root.get("deleted"))); //silinmemiş olanları getir demek.

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}