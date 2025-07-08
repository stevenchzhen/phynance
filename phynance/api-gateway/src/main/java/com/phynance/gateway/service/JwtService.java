package com.phynance.gateway.service;

import com.phynance.gateway.model.JwtToken;
import com.phynance.gateway.model.TokenType;
import com.phynance.gateway.model.User;
import com.phynance.gateway.repository.JwtTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String jwtSecret;
    
    @Value("${jwt.access-token.expiration:900}") // 15 minutes in seconds
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token.expiration:604800}") // 7 days in seconds
    private long refreshTokenExpiration;
    
    private final JwtTokenRepository jwtTokenRepository;
    
    public JwtService(JwtTokenRepository jwtTokenRepository) {
        this.jwtTokenRepository = jwtTokenRepository;
    }
    
    public String generateAccessToken(UserDetails userDetails, String deviceFingerprint, String ipAddress, String userAgent) {
        return generateToken(userDetails, TokenType.ACCESS, deviceFingerprint, ipAddress, userAgent);
    }
    
    public String generateRefreshToken(UserDetails userDetails, String deviceFingerprint, String ipAddress, String userAgent) {
        return generateToken(userDetails, TokenType.REFRESH, deviceFingerprint, ipAddress, userAgent);
    }
    
    private String generateToken(UserDetails userDetails, TokenType tokenType, String deviceFingerprint, String ipAddress, String userAgent) {
        String tokenId = UUID.randomUUID().toString();
        long expiration = tokenType == TokenType.ACCESS ? accessTokenExpiration : refreshTokenExpiration;
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenId", tokenId);
        claims.put("tokenType", tokenType.name());
        claims.put("deviceFingerprint", deviceFingerprint);
        claims.put("ipAddress", ipAddress);
        
        String token = createToken(claims, userDetails.getUsername(), expiration);
        
        // Store token in database
        JwtToken jwtToken = new JwtToken();
        jwtToken.setTokenId(tokenId);
        jwtToken.setUser((User) userDetails);
        jwtToken.setTokenType(tokenType);
        jwtToken.setExpiresAt(LocalDateTime.now().plusSeconds(expiration));
        jwtToken.setDeviceFingerprint(deviceFingerprint);
        jwtToken.setIpAddress(ipAddress);
        jwtToken.setUserAgent(userAgent);
        
        jwtTokenRepository.save(jwtToken);
        
        logger.info("Generated {} token for user: {}", tokenType, userDetails.getUsername());
        return token;
    }
    
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractTokenId(String token) {
        return extractClaim(token, claims -> claims.get("tokenId", String.class));
    }
    
    public TokenType extractTokenType(String token) {
        String tokenTypeStr = extractClaim(token, claims -> claims.get("tokenType", String.class));
        return TokenType.valueOf(tokenTypeStr);
    }
    
    public String extractDeviceFingerprint(String token) {
        return extractClaim(token, claims -> claims.get("deviceFingerprint", String.class));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public boolean isTokenBlacklisted(String tokenId) {
        return jwtTokenRepository.findByTokenId(tokenId)
                .map(JwtToken::isBlacklisted)
                .orElse(true); // Consider unknown tokens as blacklisted
    }
    
    public boolean validateToken(String token, UserDetails userDetails) {
        long startTime = System.currentTimeMillis();
        try {
            final String username = extractUsername(token);
            final String tokenId = extractTokenId(token);
            
            // Check if token is blacklisted
            if (isTokenBlacklisted(tokenId)) {
                logger.warn("Token {} is blacklisted for user: {}", tokenId, username);
                return false;
            }
            
            // Check if token is expired
            if (isTokenExpired(token)) {
                logger.warn("Token {} is expired for user: {}", tokenId, username);
                return false;
            }
            
            // Check if username matches
            if (!username.equals(userDetails.getUsername())) {
                logger.warn("Token username mismatch for token: {}", tokenId);
                return false;
            }
            
            long validationTime = System.currentTimeMillis() - startTime;
            
            // Performance monitoring for JWT validation
            if (validationTime > 50) {
                logger.warn("Slow JWT validation detected: {}ms for user: {}", validationTime, username);
            } else {
                logger.debug("JWT validation completed in {}ms for user: {}", validationTime, username);
            }
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            long validationTime = System.currentTimeMillis() - startTime;
            logger.error("Token validation failed in {}ms: {}", validationTime, e.getMessage());
            return false;
        }
    }
    
    public void blacklistToken(String tokenId) {
        jwtTokenRepository.findByTokenId(tokenId).ifPresent(token -> {
            token.setBlacklisted(true);
            jwtTokenRepository.save(token);
            logger.info("Token {} blacklisted for user: {}", tokenId, token.getUser().getUsername());
        });
    }
    
    public void blacklistAllUserTokens(User user) {
        jwtTokenRepository.blacklistAllUserTokens(user);
        logger.info("All tokens blacklisted for user: {}", user.getUsername());
    }
    
    public void blacklistUserTokensByDevice(User user, String deviceFingerprint) {
        jwtTokenRepository.blacklistUserTokensByDevice(user, deviceFingerprint);
        logger.info("Device tokens blacklisted for user: {} and device: {}", user.getUsername(), deviceFingerprint);
    }
    
    public long getActiveTokenCount(User user) {
        return jwtTokenRepository.countActiveTokensByUser(user, LocalDateTime.now());
    }
    
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        jwtTokenRepository.deleteExpiredTokens(now);
        logger.info("Cleaned up expired tokens");
    }
} 