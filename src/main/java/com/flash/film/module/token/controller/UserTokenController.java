package com.flash.film.module.token.controller;

import com.flash.film.common.dto.ApiResponse;
import com.flash.film.common.enums.AppCode;
import com.flash.film.module.token.entity.UserToken;
import com.flash.film.module.token.repository.UserTokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/film/token/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Token Management", description = "Manage and revoke JWT tokens")
public class UserTokenController {

    private final UserTokenRepository userTokenRepository;

    @GetMapping("/list")
    @Operation(summary = "List all tokens for current user")
    public ResponseEntity<ApiResponse<List<UserToken>>> listMyTokens(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserToken> tokens = userTokenRepository.findAllByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(tokens, AppCode.SUCCESS, "OK"));
    }

    @GetMapping("/active")
    @Operation(summary = "List active (non-revoked) tokens for current user")
    public ResponseEntity<ApiResponse<List<UserToken>>> listActiveTokens(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        List<UserToken> tokens = userTokenRepository.findActiveTokensByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(tokens, AppCode.SUCCESS, "OK"));
    }

    @DeleteMapping("/{id}/revoke")
    @Operation(summary = "Revoke a specific token by ID")
    public ResponseEntity<ApiResponse<Void>> revokeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        userTokenRepository.findById(id).ifPresent(token -> {
            if (token.getUserId().equals(userId) && !token.getIsRevoked()) {
                token.setIsRevoked(true);
                token.setRevokedAt(new Timestamp(System.currentTimeMillis()));
                token.setRevokeReason("MANUAL_REVOKE");
                token.setUpdatedBy(userId.toString());
                userTokenRepository.save(token);
            }
        });
        return ResponseEntity.ok(ApiResponse.ok(null, AppCode.SUCCESS, "Token revoked"));
    }

    @DeleteMapping("/revoke-all")
    @Operation(summary = "Revoke all tokens for current user")
    public ResponseEntity<ApiResponse<Void>> revokeAll(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        int count = userTokenRepository.revokeAllByUserId(
                userId, new Timestamp(System.currentTimeMillis()), "REVOKE_ALL");
        return ResponseEntity.ok(ApiResponse.ok(null, AppCode.SUCCESS,
                "Revoked " + count + " token(s)"));
    }
}
