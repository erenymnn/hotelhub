package com.example.hotelhub.mapper;

import com.example.hotelhub.dto.request.HotelRequest;
import com.example.hotelhub.dto.response.HotelResponse;
import com.example.hotelhub.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface HotelMapper {

    HotelResponse toResponse(Hotel hotel); //dbden gelen veriyi jsona çevirir.

    @Mapping(target = "id", ignore = true)//burada yani hotel ve hotelrequest dtosunda name var eşler otomatik
    @Mapping(target = "deleted", ignore = true) //eğer true yazmazsak hotelrequest istek gonderdigimizde 5 numaralı oteli güncellerken id alanına 1 yazar veritabanı darmadagın olur.
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    Hotel toEntity(HotelRequest request); //apiden gelen istegi dbye kaydedilebilir şekle cevirir.

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    void updateEntityFromRequest(HotelRequest request, @MappingTarget Hotel hotel); //bu MappingTarget yeni bir nesne olusturma hotel veya roomdan geleni request bilgisi ile güncelle.
}// bu ise var olan bilgiyi yenisi ile günceller.
//ignore = true demek: "Bu alanı kullanıcı değiştiremez, buraya dokunma, benim (backend'in) yönettiğim bir alan bu!" demektir.