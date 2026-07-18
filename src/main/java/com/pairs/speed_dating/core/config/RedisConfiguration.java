package com.pairs.speed_dating.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.RedisClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfiguration {

  private final RedisProperties properties;

  @Bean
  public RedisClient initRedisClient(){
    ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
    poolConfig.setMaxTotal(10);
    poolConfig.setMaxIdle(5);
    poolConfig.setMinIdle(1);

    JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
      .password(properties.password())
      .timeoutMillis(2000)
      .build();

    RedisClient client = RedisClient.builder()
      .hostAndPort(properties.host(), properties.port())
      .clientConfig(clientConfig)
      .poolConfig(poolConfig)
      .build();

    try {
      client.ping();
      log.info("Connected to Redis");
      return client;
    } catch (Exception e) {
      log.error("Failed to connect to Redis", e);
      throw new RuntimeException("Can't connect to Redis");
    }
  }
}
