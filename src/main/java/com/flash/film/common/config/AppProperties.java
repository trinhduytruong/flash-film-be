package com.flash.film.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String apiPrefix = "film";

    private Jwt jwt = new Jwt();

    private Scheduler scheduler = new Scheduler();

    @Getter
    @Setter
    public static class Scheduler {
        private TokenCleanup tokenCleanup = new TokenCleanup();

        @Getter
        @Setter
        public static class TokenCleanup {
            private boolean enabled;
            private int daysPastExpiry;
            private String cron;
        }
    }

    @Getter
    @Setter
    public static class Jwt {
        private long accessTokenExpirationMs = 900_000L;
        private long refreshTokenExpirationMs = 604_800_000L;
        private boolean refreshEnabled = true;
    }
}
