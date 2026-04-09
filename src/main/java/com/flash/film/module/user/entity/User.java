package com.flash.film.module.user.entity;

import com.flash.film.common.entity.BaseEntity;
import com.flash.film.common.enums.Gender;
import com.flash.film.common.enums.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;


/**
 * Bảng user_profiles — lưu thông tin người dùng.
 * Auth do Keycloak quản lý.
 */
@Getter
@Setter
@Entity
@Table(name = "user_profiles", indexes = {
        @Index(name = "idx_up_keycloak_id", columnList = "keycloak_id", unique = true),
        @Index(name = "idx_up_username", columnList = "username", unique = true),
        @Index(name = "idx_up_email", columnList = "email", unique = true)
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 36)
    private String keycloakId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "company", length = 200)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType = UserType.USER;

    @Column(name = "last_login")
    private Timestamp lastLogin;
}
