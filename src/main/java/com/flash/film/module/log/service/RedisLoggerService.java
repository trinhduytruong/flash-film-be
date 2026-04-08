package com.flash.film.module.log.service;

import java.util.Map;

public interface RedisLoggerService {
    void logRedisAsync(Map<String, Object> logData);
}
