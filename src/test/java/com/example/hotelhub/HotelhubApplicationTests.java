package com.example.hotelhub;

import com.example.hotelhub.elasticsearch.HotelElasticRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class HotelhubApplicationTests {

	@MockitoBean
	private HotelElasticRepository hotelElasticRepository;

	@Test
	void contextLoads() {
	}

}
