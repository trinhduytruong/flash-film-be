package com.flash.film.module.rabbitmq.repository;

import com.flash.film.module.rabbitmq.entity.RabbitmqConnectionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RabbitmqConnectionLogRepository extends JpaRepository<RabbitmqConnectionLog, Long> {
}
