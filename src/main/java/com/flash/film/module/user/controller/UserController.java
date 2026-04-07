package com.flash.film.module.user.controller;

import com.flash.film.common.config.security.CustomUserDetails;
import com.flash.film.common.dto.ApiResponse;
import com.flash.film.module.user.dto.UpdateProfileRequest;
import com.flash.film.module.user.dto.UserProfileResponse;
import com.flash.film.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/film/user/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = principal.getUserId();
        
        ApiResponse<String> response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = principal.getUserId();
        
        ApiResponse<UserProfileResponse> response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
}
