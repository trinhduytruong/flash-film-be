package com.flash.film.module.redis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Bảng log Redis — track các Redis operation và lỗi kết nối.
 */
@Getter
@Setter
@Entity
@Table(name = "redis_connection_log", indexes = {
        @Index(name = "idx_redis_log_executed_at", columnList = "executed_at"),
        @Index(name = "idx_redis_log_status", columnList = "status")
})
public class RedisConnectionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** GET, SET, DELETE, EXISTS, EXPIRE, ... */
    @Column(name = "operation", length = 30)
    private String operation;

    @Column(name = "redis_key", length = 300)
    private String redisKey;

    /** SUCCESS / FAILED */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "executed_at")
    private Timestamp executedAt;
}
