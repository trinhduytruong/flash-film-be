package com.flash.film.module.log.service;

import java.util.Map;

public interface RabbitmqLoggerService {
    void logRabbitmqAsync(Map<String, Object> logData);
}
