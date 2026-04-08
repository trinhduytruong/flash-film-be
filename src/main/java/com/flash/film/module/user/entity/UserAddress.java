package com.flash.film.module.user.entity;

import com.flash.film.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_addresses", indexes = {
        @Index(name = "idx_user_addresses_user_id", columnList = "user_id")
})
public class UserAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_default", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDefault = false;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "apartment_suite", length = 255)
    private String apartmentSuite;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "zip_code", length = 50)
    private String zipCode;
}
