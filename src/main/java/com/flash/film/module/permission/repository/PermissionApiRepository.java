package com.flash.film.module.permission.repository;

import com.flash.film.common.enums.UserType;
import com.flash.film.module.permission.entity.PermissionApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionApiRepository extends JpaRepository<PermissionApi, Long> {

    @Query("SELECT p FROM PermissionApi p WHERE p.userType = :userType AND p.isActive = true")
    List<PermissionApi> findActiveByUserType(@Param("userType") UserType userType);

    @Query("SELECT p FROM PermissionApi p WHERE p.userType = :userType " +
           "AND p.httpMethod = :method AND p.isActive = true")
    List<PermissionApi> findActiveByUserTypeAndMethod(@Param("userType") UserType userType,
                                                       @Param("method") String method);
}
