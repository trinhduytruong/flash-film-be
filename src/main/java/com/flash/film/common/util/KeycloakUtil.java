package com.flash.film.common.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class KeycloakUtil {

    private KeycloakUtil() {
        // Utility class
    }

    public static String getKeycloakId(Jwt jwt) {
        return jwt.getSubject();
    }

    public static String getUsername(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }

    public static String getEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }

    public static String getFirstName(Jwt jwt) {
        return jwt.getClaimAsString("given_name");
    }

    public static String getLastName(Jwt jwt) {
        return jwt.getClaimAsString("family_name");
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }
        return (List<String>) realmAccess.get("roles");
    }
}
