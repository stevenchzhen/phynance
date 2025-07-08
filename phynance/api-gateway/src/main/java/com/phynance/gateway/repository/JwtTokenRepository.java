package com.phynance.gateway.repository;

import com.phynance.gateway.model.JwtToken;
import com.phynance.gateway.model.TokenType;
import com.phynance.gateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    
    Optional<JwtToken> findByTokenId(String tokenId);
    
    List<JwtToken> findByUserAndTokenType(User user, TokenType tokenType);
    
    List<JwtToken> findByUserAndBlacklistedFalse(User user);
    
    @Query("SELECT t FROM JwtToken t WHERE t.user = :user AND t.blacklisted = false AND t.expiresAt > :now")
    List<JwtToken> findActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(t) FROM JwtToken t WHERE t.user = :user AND t.blacklisted = false AND t.expiresAt > :now")
    long countActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE JwtToken t SET t.blacklisted = true WHERE t.user = :user")
    void blacklistAllUserTokens(@Param("user") User user);
    
    @Modifying
    @Query("UPDATE JwtToken t SET t.blacklisted = true WHERE t.user = :user AND t.deviceFingerprint = :deviceFingerprint")
    void blacklistUserTokensByDevice(@Param("user") User user, @Param("deviceFingerprint") String deviceFingerprint);
    
    @Modifying
    @Query("DELETE FROM JwtToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM JwtToken t WHERE t.expiresAt < :now")
    List<JwtToken> findExpiredTokens(@Param("now") LocalDateTime now);
} 