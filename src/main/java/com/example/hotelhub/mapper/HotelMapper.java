package com.example.hotelhub.mapper;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface HotelMapper {

    HotelResponse toResponse(Hotel hotel);

    Hotel toEntity(HotelRequest request);

    void updateEntityFromRequest(HotelRequest request, @MappingTarget Hotel hotel);
}
