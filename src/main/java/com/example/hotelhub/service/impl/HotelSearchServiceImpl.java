package com.example.hotelhub.service.impl;

import com.example.hotelhub.dto.request.HotelSearchRequest;
import com.example.hotelhub.dto.response.PageResponse;
import com.example.hotelhub.elasticsearch.HotelDocument;
import com.example.hotelhub.elasticsearch.HotelElasticRepository;
import com.example.hotelhub.service.HotelSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelSearchServiceImpl implements HotelSearchService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final HotelElasticRepository hotelElasticRepository;

    @Override
    public Page<HotelDocument> searchInElasticsearch(HotelSearchRequest request) {
        Pageable pageable = PageRequest.of(resolvePage(request.page()), resolveSize(request.size()));

        if (!hasSearchText(request)) {
            return hotelElasticRepository.findAll(pageable);
        }

        return hotelElasticRepository.searchAcrossAllFields(buildSearchText(request), pageable);
    }

    @Override
    // Anasayfa vitrini için ES'yi yormuyoruz. "topRated" adında bir cache oluşturuyoruz.
    // sync = true diyerek, aynı anda binlerce kişi anasayfaya girse bile ES'ye tek bir sorgu gitmesini sağlıyoruz (Stampede Koruması).
    @Cacheable(value = "topRatedHotels", key = "#size", sync = true)
    public PageResponse<HotelDocument> getTopRatedHotels(int size) {
        // En yüksek puanlıları getirmek için rating alanına göre azalan sıralama (DESC)
        Pageable pageable = PageRequest.of(0, Math.min(size, MAX_SIZE), Sort.by(Sort.Direction.DESC, "rating"));
        Page<HotelDocument> page = hotelElasticRepository.findAll(pageable);
        return PageResponse.of(page, page.getContent());
    }

    // Yardımcı Metotlar
    private int resolvePage(Integer page) { return (page == null || page < 0) ? DEFAULT_PAGE : page; }
    private int resolveSize(Integer size) { return (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE); }
    private boolean hasSearchText(HotelSearchRequest request) {
        return StringUtils.hasText(request.city()) || StringUtils.hasText(request.district()) || StringUtils.hasText(request.description());
    }
    private String buildSearchText(HotelSearchRequest request) {
        return Stream.of(request.city(), request.district(), request.description())
                .filter(StringUtils::hasText).map(String::trim).collect(Collectors.joining(" "));
    }
}