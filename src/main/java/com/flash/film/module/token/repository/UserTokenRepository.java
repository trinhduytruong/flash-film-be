package com.flash.film.module.token.repository;

import com.flash.film.module.token.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByAccessTokenAndIsRevokedFalse(String accessToken);

    Optional<UserToken> findByRefreshTokenAndIsRevokedFalse(String refreshToken);

    @Query("SELECT t FROM UserToken t WHERE t.userId = :userId AND t.isRevoked = false")
    List<UserToken> findActiveTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM UserToken t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    List<UserToken> findAllByUserId(@Param("userId") Long userId);

    /** Revoke tất cả token của một user (dùng khi đổi password hoặc admin revoke) */
    @Modifying
    @Transactional
    @Query("UPDATE UserToken t SET t.isRevoked = true, t.revokedAt = :revokedAt, t.revokeReason = :reason " +
           "WHERE t.userId = :userId AND t.isRevoked = false")
    int revokeAllByUserId(@Param("userId") Long userId,
                          @Param("revokedAt") Timestamp revokedAt,
                          @Param("reason") String reason);

    /** Xóa cứng các token mà refresh_token đã hết hạn */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserToken t WHERE t.refreshExpiresAt <= :thresholdDate")
    int deleteExpiredRefreshTokens(@Param("thresholdDate") Timestamp thresholdDate);
}
