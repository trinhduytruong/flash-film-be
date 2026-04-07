package com.flash.film.module.token.scheduler;

import com.flash.film.common.config.AppProperties;
import com.flash.film.module.token.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.token-cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class UserTokenCleanupScheduler {

    private final UserTokenRepository userTokenRepository;
    private final AppProperties appProperties;

    @Scheduled(cron = "${app.scheduler.token-cleanup.cron}")
    public void cleanupExpiredTokens() {
        if (!appProperties.getScheduler().getTokenCleanup().isEnabled()) {
            return;
        }

        int daysPastExpiry = appProperties.getScheduler().getTokenCleanup().getDaysPastExpiry();
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysPastExpiry);
        Timestamp thresholdTimestamp = Timestamp.valueOf(threshold);

        log.info("Start cleaning up UserTokens. Permanently delete tokens with expired refresh_token: {}", thresholdTimestamp);

        int deletedCount = userTokenRepository.deleteExpiredRefreshTokens(thresholdTimestamp);

        log.info("Cleanup complete. Expired {} tokens have been permanently deleted.", deletedCount);
    }
}
