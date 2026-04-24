package com.example.hotelhub.mapper;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = {RoomMapper.class})
public interface HotelMapper {
    // MapStruct burada hotel içindeki List<Room>'u otomatik olarak
    // RoomMapper'ı kullanarak List<RoomResponse>'a çevirecektir.
    HotelResponse toResponse(Hotel hotel);
    Hotel toEntity(HotelRequest request);
    // YENİ: Dışarıdan gelen Request'i al, içerideki (veritabanından bulduğumuz) Hotel nesnesinin üzerine yaz!
    void updateEntityFromRequest(HotelRequest request, @MappingTarget Hotel hotel);
}
