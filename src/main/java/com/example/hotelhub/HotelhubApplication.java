package com.example.hotelhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.example.hotelhub.elasticsearch")
// Spring'e "Sayfalama verilerini güvenli ve sabit bir DTO yapısına çevir" diyoruz:
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableAsync
public class HotelhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelhubApplication.class, args);
	}

}
