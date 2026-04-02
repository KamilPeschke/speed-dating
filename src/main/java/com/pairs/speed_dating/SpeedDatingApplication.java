package com.pairs.speed_dating;

import com.pairs.speed_dating.redis.RedisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RedisProperties.class)
public class SpeedDatingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpeedDatingApplication.class, args);
	}

}
