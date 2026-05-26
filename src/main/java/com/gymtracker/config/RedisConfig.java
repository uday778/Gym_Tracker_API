package com.gymtracker.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Custom Redis JSON serializer
     * Supports:
     * - LocalDate
     * - LocalDateTime
     * - Instant
     * - Type-safe serialization/deserialization
     */
    @Bean
    public GenericJackson2JsonRedisSerializer redisSerializer() {

        ObjectMapper objectMapper = new ObjectMapper();

        // Support Java 8 Date/Time API
        objectMapper.registerModule(new JavaTimeModule());

        // Preserve class type info
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * RedisTemplate configuration
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer redisSerializer
    ) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // String keys
        template.setKeySerializer(new StringRedisSerializer());

        // JSON values
        template.setValueSerializer(redisSerializer);

        // Hash key/value serializers
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(redisSerializer);

        template.afterPropertiesSet();

        return template;
    }

    /**
     * Default cache configuration
     */
    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration(
            GenericJackson2JsonRedisSerializer redisSerializer
    ) {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(redisSerializer)
                );
    }

    /**
     * Cache manager with per-cache TTL
     */
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration defaultCacheConfiguration
    ) {

        // Cache subscription tiers for 1 hour
        RedisCacheConfiguration subscriptionTiersConfig =
                defaultCacheConfiguration.entryTtl(Duration.ofHours(1));

        // Cache workout templates for 30 minutes
        RedisCacheConfiguration workoutTemplatesConfig =
                defaultCacheConfiguration.entryTtl(Duration.ofMinutes(30));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfiguration)
                .withCacheConfiguration("subscriptionTiers", subscriptionTiersConfig)
                .withCacheConfiguration("workoutTemplates", workoutTemplatesConfig)
                .transactionAware()
                .build();
    }
}