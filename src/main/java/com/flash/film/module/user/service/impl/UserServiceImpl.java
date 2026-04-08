package com.flash.film.module.user.service.impl;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.module.user.dto.AddressDto;
import com.flash.film.module.user.dto.AddressResponse;
import com.flash.film.module.user.dto.UpdateProfileRequest;
import com.flash.film.module.user.dto.UserProfileResponse;
import com.flash.film.module.user.entity.User;
import com.flash.film.module.user.entity.UserAddress;
import com.flash.film.module.user.repository.UserAddressRepository;
import com.flash.film.module.user.repository.UserRepository;
import com.flash.film.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setCompany(request.getCompany());

        userRepository.save(user);

        // Update default address
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

        List<AddressResponse> addresses = userAddressRepository.findByUserId(userId)
                .stream()
                .map(this::toAddressResponse)
                .toList();

        UserProfileResponse response = UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .company(user.getCompany())
                .addresses(addresses)
                .build();

        return ApiResponse.ok(response, AppCode.SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AddressResponse>> getAddresses(Long userId) {
        List<AddressResponse> addresses = userAddressRepository.findByUserId(userId)
                .stream()
                .map(this::toAddressResponse)
                .toList();
        return ApiResponse.ok(addresses, AppCode.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<AddressResponse> addAddress(Long userId, AddressDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        boolean hasDefault = userAddressRepository.findByUserIdAndIsDefaultTrue(userId).isPresent();

        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setIsDefault(!hasDefault);
        address.setCountry(request.getCountry());
        address.setAddressLine(request.getAddressLine());
        address.setApartmentSuite(request.getApartmentSuite());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());

        UserAddress saved = userAddressRepository.save(address);

        log.info("Address added for user id: {}", userId);
        return ApiResponse.ok(toAddressResponse(saved), AppCode.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<AddressResponse> updateAddress(Long userId, Long addressId, AddressDto request) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new CustomException(AppCode.NOT_FOUND, HttpStatus.NOT_FOUND,
                        "Address not found"));

        address.setCountry(request.getCountry());
        address.setAddressLine(request.getAddressLine());
        address.setApartmentSuite(request.getApartmentSuite());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());

        UserAddress saved = userAddressRepository.save(address);

        log.info("Address {} updated for user id: {}", addressId, userId);
        return ApiResponse.ok(toAddressResponse(saved), AppCode.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new CustomException(AppCode.NOT_FOUND, HttpStatus.NOT_FOUND,
                        "Address not found"));

        // Nếu xóa địa chỉ default → tự động promote địa chỉ khác lên default
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            userAddressRepository.findByUserId(userId).stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .findFirst()
                    .ifPresent(next -> {
                        next.setIsDefault(true);
                        userAddressRepository.save(next);
                    });
        }

        userAddressRepository.delete(address);

        log.info("Address {} deleted for user id: {}", addressId, userId);
        return ApiResponse.ok("Address deleted successfully", AppCode.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<String> setDefaultAddress(Long userId, Long addressId) {
        UserAddress newDefault = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new CustomException(AppCode.NOT_FOUND, HttpStatus.NOT_FOUND,
                        "Address not found"));

        // Unset địa chỉ default cũ
        userAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(old -> {
                    old.setIsDefault(false);
                    userAddressRepository.save(old);
                });

        // Set default mới
        newDefault.setIsDefault(true);
        userAddressRepository.save(newDefault);

        log.info("Address {} set as default for user id: {}", addressId, userId);
        return ApiResponse.ok("Default address updated successfully", AppCode.SUCCESS);
    }

    private AddressResponse toAddressResponse(UserAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .isDefault(address.getIsDefault())
                .country(address.getCountry())
                .addressLine(address.getAddressLine())
                .apartmentSuite(address.getApartmentSuite())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .build();
    }
}
