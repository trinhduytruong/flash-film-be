package com.flash.film.module.log.repository;

import com.flash.film.module.log.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    @Query("SELECT a FROM AccessLog a WHERE a.userId = :userId ORDER BY a.requestAt DESC")
    List<AccessLog> findByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM AccessLog a WHERE a.requestAt BETWEEN :from AND :to ORDER BY a.requestAt DESC")
    List<AccessLog> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
