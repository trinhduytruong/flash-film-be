package com.flash.film.module.user.dto;

import com.flash.film.common.enums.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@Getter
@Setter
@Builder
public class UserProfileResponse {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Gender gender;
    private Date dateOfBirth;
    private String company;
    private List<AddressResponse> addresses;
}
