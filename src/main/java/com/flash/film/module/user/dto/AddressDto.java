package com.flash.film.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {

    @NotBlank(message = "Country/Region is required")
    private String country;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    private String apartmentSuite;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Zip code is required")
    private String zipCode;
}
