package com.flash.film.module.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private final String accessToken;

    @JsonIgnore
    private final String refreshToken;

    private final Boolean refreshEnabled;

    private final String userType;
}
