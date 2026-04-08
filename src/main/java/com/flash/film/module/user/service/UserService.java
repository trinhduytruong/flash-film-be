package com.flash.film.module.user.service;

import com.flash.film.module.user.dto.AddressDto;
import com.flash.film.module.user.dto.AddressResponse;
import com.flash.film.module.user.dto.UpdateProfileRequest;
import com.flash.film.common.dto.ApiResponse;

import com.flash.film.module.user.dto.UserProfileResponse;

import java.util.List;

public interface UserService {
    ApiResponse<String> updateProfile(Long userId, UpdateProfileRequest request);
    ApiResponse<UserProfileResponse> getProfile(Long userId);
    ApiResponse<List<AddressResponse>> getAddresses(Long userId);
    ApiResponse<AddressResponse> addAddress(Long userId, AddressDto request);
    ApiResponse<AddressResponse> updateAddress(Long userId, Long addressId, AddressDto request);
    ApiResponse<String> deleteAddress(Long userId, Long addressId);
    ApiResponse<String> setDefaultAddress(Long userId, Long addressId);
}

