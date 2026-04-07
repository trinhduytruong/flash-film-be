package com.flash.film.module.log.service;

import java.util.Map;

public interface AccessLoggerService {
    void logAccessAsync(Map<String, Object> logData);
}
