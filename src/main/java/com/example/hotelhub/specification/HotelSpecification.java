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

            if (request.minRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("rating"),
                        request.minRating()
                ));
            }

            if (request.maxPrice() != null) {
                Join<Hotel, Room> roomsJoin = root.join("rooms");

                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        roomsJoin.get("pricePerNight"),
                        request.maxPrice()
                ));

                query.distinct(true);
            }

            if (request.checkInDate() != null && request.checkOutDate() != null) {
                Join<Hotel, Room> roomJoin = root.join("rooms");

                Subquery<Long> busyRoomsQuery = query.subquery(Long.class);
                Root<Booking> bookingRoot = busyRoomsQuery.from(Booking.class);

                busyRoomsQuery.select(bookingRoot.get("room").get("id"));

                Predicate overlapCondition = criteriaBuilder.and(
                        criteriaBuilder.lessThan(bookingRoot.get("checkInDate"), request.checkOutDate()),
                        criteriaBuilder.greaterThan(bookingRoot.get("checkOutDate"), request.checkInDate()),
                        criteriaBuilder.equal(bookingRoot.get("status"), BookingStatus.CONFIRMED)
                );
                busyRoomsQuery.where(overlapCondition);

                predicates.add(criteriaBuilder.not(roomJoin.get("id").in(busyRoomsQuery)));

                query.distinct(true);
            }

            predicates.add(criteriaBuilder.isFalse(root.get("is_Deleted")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}