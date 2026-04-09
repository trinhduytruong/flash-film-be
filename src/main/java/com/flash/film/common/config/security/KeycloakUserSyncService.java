package com.flash.film.common.config.security;

import com.flash.film.module.user.entity.User;
import com.flash.film.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flash.film.common.util.KeycloakUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public User syncUser(Jwt jwt) {
        String keycloakId = KeycloakUtil.getKeycloakId(jwt);
        String username = KeycloakUtil.getUsername(jwt);
        String email = KeycloakUtil.getEmail(jwt);
        String firstName = KeycloakUtil.getFirstName(jwt);
        String lastName = KeycloakUtil.getLastName(jwt);

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            // Cập nhật thông tin cơ bản nếu có thay đổi từ Keycloak
            User user = existingUser.get();
            boolean changed = false;
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                changed = true;
            }
            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(username);
                changed = true;
            }
            if (changed) {
                return userRepository.save(user);
            }
            return user;
        }

        // Tạo mới profile nếu chưa có
        log.info("Provisioning new local user profile for keycloakId: {}", keycloakId);
        User newUser = new User();
        newUser.setKeycloakId(keycloakId);
        newUser.setUsername(username != null ? username : keycloakId);
        newUser.setEmail(email != null ? email : keycloakId + "@placeholder.com");
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setCreatedAt(Timestamp.from(Instant.now()));
        newUser.setIsActive(true);

        return userRepository.save(newUser);
    }
}
