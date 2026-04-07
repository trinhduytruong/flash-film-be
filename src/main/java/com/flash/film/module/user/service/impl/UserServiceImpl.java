package com.flash.film.module.user.service.impl;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.common.enums.AppCode;
import com.flash.film.module.user.dto.AddressDto;
import com.flash.film.module.user.dto.UpdateProfileRequest;
import com.flash.film.module.user.dto.UserProfileResponse;
import com.flash.film.module.user.entity.User;
import com.flash.film.module.user.entity.UserAddress;
import com.flash.film.module.user.repository.UserAddressRepository;
import com.flash.film.module.user.repository.UserRepository;
import com.flash.film.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Override
    @Transactional
    public ApiResponse<String> updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 1. Update Core Profile
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setCompany(request.getCompany());

        userRepository.save(user);

        // 2. Update Default Address
        AddressDto addressDto = request.getAddress();
        UserAddress defaultAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseGet(() -> {
                    UserAddress newAddress = new UserAddress();
                    newAddress.setUser(user);
                    newAddress.setIsDefault(true);
                    return newAddress;
                });

        defaultAddress.setCountry(addressDto.getCountry());
        defaultAddress.setAddressLine(addressDto.getAddressLine());
        defaultAddress.setApartmentSuite(addressDto.getApartmentSuite());
        defaultAddress.setCity(addressDto.getCity());
        defaultAddress.setState(addressDto.getState());
        defaultAddress.setZipCode(addressDto.getZipCode());

        userAddressRepository.save(defaultAddress);

        log.info("Profile successfully updated for user id: {}", userId);
        return ApiResponse.ok("Profile updated successfully", AppCode.SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserProfileResponse response = UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .company(user.getCompany())
                .build();

        userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .ifPresent(address -> {
                    AddressDto addressDto = new AddressDto();
                    addressDto.setCountry(address.getCountry());
                    addressDto.setAddressLine(address.getAddressLine());
                    addressDto.setApartmentSuite(address.getApartmentSuite());
                    addressDto.setCity(address.getCity());
                    addressDto.setState(address.getState());
                    addressDto.setZipCode(address.getZipCode());
                    response.setAddress(addressDto);
                });

        return ApiResponse.ok(response, AppCode.SUCCESS);
    }
}
