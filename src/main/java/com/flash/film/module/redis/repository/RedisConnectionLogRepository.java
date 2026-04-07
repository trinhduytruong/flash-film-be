package com.flash.film.module.redis.repository;

import com.flash.film.module.redis.entity.RedisConnectionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisConnectionLogRepository extends JpaRepository<RedisConnectionLog, Long> {
}
