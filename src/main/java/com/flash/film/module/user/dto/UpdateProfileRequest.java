package com.flash.film.module.user.dto;

import com.flash.film.common.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{7,20}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    private Date dateOfBirth;

    private String company;

    @Valid
    @NotNull(message = "Address information is required")
    private AddressDto address;
}
