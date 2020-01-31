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

//	@Bean
	public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig)  {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		config.setHostName("localhost");
		config.setPort(6379);
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
		return jedisConnectionFactory;
	}

}
