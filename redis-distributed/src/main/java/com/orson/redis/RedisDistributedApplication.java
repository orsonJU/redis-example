package com.orson.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

@SpringBootApplication
public class RedisDistributedApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisDistributedApplication.class, args);
	}

}
