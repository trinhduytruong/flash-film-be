package com.flash.film.module.rabbitmq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Bảng log RabbitMQ — track các message queue event.
 */
@Getter
@Setter
@Entity
@Table(name = "rabbitmq_connection_log", indexes = {
        @Index(name = "idx_rmq_log_executed_at", columnList = "executed_at"),
        @Index(name = "idx_rmq_log_status", columnList = "status"),
        @Index(name = "idx_rmq_log_queue", columnList = "queue_name")
})
public class RabbitmqConnectionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exchange_name", length = 200)
    private String exchangeName;

    @Column(name = "queue_name", length = 200)
    private String queueName;

    @Column(name = "routing_key", length = 200)
    private String routingKey;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "message_body", columnDefinition = "TEXT")
    private String messageBody;

    /** PUBLISHED / CONSUMED / FAILED / DEAD_LETTER */
    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "executed_at")
    private Timestamp executedAt;
}
