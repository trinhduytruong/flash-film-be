package com.flash.film.module.user.service;

import com.flash.film.common.enums.AppCode;
import com.flash.film.common.exception.CustomException;
import com.flash.film.common.config.security.CustomUserDetails;
import com.flash.film.module.user.entity.User;
import com.flash.film.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return buildUserDetails(user);
    }

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findActiveById(id)
                .orElseThrow(() -> new CustomException(
                        AppCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "User not found: " + id));
        return buildUserDetails(user);
    }

    /** Lấy raw User entity (cần jwtSecret cho JWT validation) */
    public User loadRawUserById(Long id) {
        return userRepository.findActiveById(id)
                .orElseThrow(() -> new CustomException(
                        AppCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "User not found: " + id));
    }

    private UserDetails buildUserDetails(User user) {
        return new CustomUserDetails(user);
    }
}
