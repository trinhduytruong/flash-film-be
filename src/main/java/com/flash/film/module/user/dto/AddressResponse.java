package com.flash.film.module.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {

    private Long id;

    private Boolean isDefault;

    private String country;

    private String addressLine;

    private String apartmentSuite;

    private String city;

    private String state;

    private String zipCode;
}
