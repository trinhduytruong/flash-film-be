package com.flash.film.common.config.security;

import com.flash.film.module.log.service.AccessLoggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.keycloak.adapters.authorization.integration.jakarta.ServletPolicyEnforcerFilter;
import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
import org.keycloak.util.JsonSerialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccessLoggerService accessLoggerService;
    private final KeycloakJwtConverter keycloakJwtConverter;
    private final KeycloakUserSyncService keycloakUserSyncService;

    private static final String[] PUBLIC_PATHS = {
            "/film/public/**",
            "/api-docs/**",
            "/redoc/**",
            "/redoc",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public KeycloakAuthFilter keycloakAuthFilter() {
        return new KeycloakAuthFilter(accessLoggerService, keycloakUserSyncService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                )
                .addFilterAfter(keycloakAuthFilter(), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(createPolicyEnforcerFilter(), KeycloakAuthFilter.class);

        return http.build();
    }

    private ServletPolicyEnforcerFilter createPolicyEnforcerFilter() {
        return new ServletPolicyEnforcerFilter(request -> {
            try {
                return JsonSerialization.readValue(
                        getClass().getResourceAsStream("/policy-enforcer.json"),
                        PolicyEnforcerConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not read policy-enforcer.json", e);
            }
        });
    }
}
