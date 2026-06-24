package com.example.hotelhub.mapper;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponse toResponse(Room room);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isAvailable", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    Room toEntity(RoomRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isAvailable", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    void updateEntityFromRequest(RoomRequest request, @MappingTarget Room room);

}
