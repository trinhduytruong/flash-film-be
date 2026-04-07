package com.flash.film.module.redis.service;

public interface RedisService {
    void set(String key, Object value, long ttlSeconds);
    Object get(String key);
    void delete(String key);
    boolean exists(String key);
    void setWithoutLog(String key, Object value, long ttlSeconds);
}
