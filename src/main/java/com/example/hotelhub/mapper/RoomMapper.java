package com.example.hotelhub.mapper;

import com.example.hotelhub.dto.request.RoomRequest;
import com.example.hotelhub.dto.response.RoomResponse;
import com.example.hotelhub.entity.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {

   RoomResponse toResponse(Room room);
   Room toEntity(RoomRequest request);

}
