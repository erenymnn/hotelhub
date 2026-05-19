package com.example.hotelhub.mapper;

import com.example.hotelhub.dto.request.BookingRequest;
import com.example.hotelhub.dto.response.BookingResponse;
import com.example.hotelhub.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    // Kaynak (source) Entity içindeki yol, Hedef (target) DTO içindeki değişken adı
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "user.id", target = "userId")
    BookingResponse toResponse(Booking booking);

    // id, room, user, totalPrice ve status dışarıdan (request'ten) gelmediği için ignore (görmezden gel) diyoruz.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(BookingRequest request);
}
