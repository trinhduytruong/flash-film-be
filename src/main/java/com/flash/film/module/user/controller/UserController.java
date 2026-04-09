package com.flash.film.module.user.controller;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.module.user.dto.AddressDto;
import com.flash.film.module.user.dto.AddressResponse;
import com.flash.film.module.user.dto.UpdateProfileRequest;
import com.flash.film.module.user.dto.UserProfileResponse;
import com.flash.film.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@RestController
@RequestMapping("/film/user/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.getAddresses(userId));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressDto request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.addAddress(userId, request));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDto request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.updateAddress(userId, id, request));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.deleteAddress(userId, id));
    }

    @PatchMapping("/addresses/default/{id}")
    public ResponseEntity<ApiResponse<String>> setDefaultAddress(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.setDefaultAddress(userId, id));
    }

    private Long getCurrentUserId() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return (Long) attr.getRequest().getAttribute("localUserId");
    }
}
