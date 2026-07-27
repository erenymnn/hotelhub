package com.example.hotelhub;

import com.example.hotelhub.elasticsearch.HotelElasticRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {"jwt.secret=9a4f2c8d3b7a1e6f45c8a0b3f267d8b1d4e6f3c8a9d2b5f8e3a9c8b5f6v8a3d9", "spring.data.redis.host=localhost", "spring.data.redis.port=6379"})
class HotelhubApplicationTests {

	@MockitoBean
	private HotelElasticRepository hotelElasticRepository;

	@Test
	void contextLoads() {
	}

}
