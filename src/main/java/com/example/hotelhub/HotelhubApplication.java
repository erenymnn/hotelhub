package com.example.hotelhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
// Spring'e "Sayfalama verilerini güvenli ve sabit bir DTO yapısına çevir" diyoruz:
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class HotelhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelhubApplication.class, args);
	}

}
