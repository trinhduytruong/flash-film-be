package com.flash.film.module.user.service;

import com.flash.film.module.user.dto.UpdateProfileRequest;
import com.flash.film.common.dto.ApiResponse;

import com.flash.film.module.user.dto.UserProfileResponse;

public interface UserService {
    ApiResponse<String> updateProfile(Long userId, UpdateProfileRequest request);
    ApiResponse<UserProfileResponse> getProfile(Long userId);
}
