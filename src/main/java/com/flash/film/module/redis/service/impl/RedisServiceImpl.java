package com.flash.film.module.redis.service.impl;

import com.flash.film.module.redis.service.RedisService;

import com.flash.film.module.redis.entity.RedisConnectionLog;
import com.flash.film.module.redis.repository.RedisConnectionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis service — wrap RedisTemplate với error logging vào DB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionLogRepository logRepository;

    public void set(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            saveLog("SET", key, "SUCCESS", null);
        } catch (Exception e) {
            log.error("Redis SET failed — key={}: {}", key, e.getMessage());
            saveLog("SET", key, "FAILED", e.getMessage());
        }
    }

    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            saveLog("GET", key, "SUCCESS", null);
            return value;
        } catch (Exception e) {
            log.error("Redis GET failed — key={}: {}", key, e.getMessage());
            saveLog("GET", key, "FAILED", e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            saveLog("DELETE", key, "SUCCESS", null);
        } catch (Exception e) {
            log.error("Redis DELETE failed — key={}: {}", key, e.getMessage());
            saveLog("DELETE", key, "FAILED", e.getMessage());
        }
    }

    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis EXISTS failed — key={}: {}", key, e.getMessage());
            saveLog("EXISTS", key, "FAILED", e.getMessage());
            return false;
        }
    }

    public void setWithoutLog(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis SET failed — key={}: {}", key, e.getMessage());
        }
    }

    private void saveLog(String operation, String key, String status, String errorMessage) {
        try {
            RedisConnectionLog log = new RedisConnectionLog();
            log.setOperation(operation);
            log.setRedisKey(key);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setExecutedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            logRepository.save(log);
        } catch (Exception e) {
            // Không để log lỗi làm crash app
        }
    }
}
