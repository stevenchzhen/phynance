package com.phynance.gateway.service;

import com.phynance.gateway.model.User;
import com.phynance.gateway.model.UserRole;
import com.phynance.gateway.model.dto.AuthResponse;
import com.phynance.gateway.model.dto.ChangePasswordRequest;
import com.phynance.gateway.model.dto.LoginRequest;
import com.phynance.gateway.model.dto.RegisterRequest;
import com.phynance.gateway.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AuthService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Value("${auth.max-failed-attempts:5}")
    private int maxFailedAttempts;
    
    @Value("${auth.lock-duration-minutes:30}")
    private int lockDurationMinutes;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;
    
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtService jwtService, 
                      AuthenticationManager authenticationManager,
                      AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
    
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            // Check if user exists and is not locked
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            
            // Check if account is locked
            if (!user.isAccountNonLocked()) {
                if (user.getLockTime() != null && 
                    user.getLockTime().plusMinutes(lockDurationMinutes).isAfter(LocalDateTime.now())) {
                    throw new LockedException("Account is locked. Try again later.");
                } else {
                    // Unlock account if lock time has expired
                    unlockAccount(user);
                }
            }
            
            // Check IP whitelist for admin accounts
            if (user.getRole() == UserRole.ADMIN && user.getIpWhitelist() != null && !user.getIpWhitelist().isEmpty()) {
                if (!user.getIpWhitelist().contains(ipAddress)) {
                    logger.warn("Admin login attempt from unauthorized IP: {} for user: {}", ipAddress, user.getUsername());
                    auditService.logSecurityEvent(user.getUsername(), "LOGIN_FAILED_IP_NOT_WHITELISTED", ipAddress, userAgent);
                    throw new BadCredentialsException("Access denied from this IP address");
                }
            }
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            User authenticatedUser = (User) authentication.getPrincipal();
            
            // Check concurrent sessions
            long activeSessions = jwtService.getActiveTokenCount(authenticatedUser);
            if (activeSessions >= authenticatedUser.getMaxConcurrentSessions()) {
                logger.warn("Too many concurrent sessions for user: {}", authenticatedUser.getUsername());
                auditService.logSecurityEvent(authenticatedUser.getUsername(), "LOGIN_FAILED_TOO_MANY_SESSIONS", ipAddress, userAgent);
                throw new BadCredentialsException("Too many active sessions. Please logout from other devices.");
            }
            
            // Handle 2FA if enabled
            if (authenticatedUser.isTwoFactorEnabled()) {
                if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                    auditService.logSecurityEvent(authenticatedUser.getUsername(), "LOGIN_FAILED_2FA_REQUIRED", ipAddress, userAgent);
                    throw new BadCredentialsException("Two-factor authentication code required");
                }
                // TODO: Implement 2FA validation
                // validateTwoFactorCode(authenticatedUser, request.getTwoFactorCode());
            }
            
            // Reset failed attempts on successful login
            resetFailedAttempts(authenticatedUser);
            
            // Update last login
            authenticatedUser.setLastLogin(LocalDateTime.now());
            userRepository.save(authenticatedUser);
            
            // Generate tokens
            String deviceFingerprint = request.getDeviceFingerprint() != null ? 
                    request.getDeviceFingerprint() : generateDeviceFingerprint(ipAddress, userAgent);
            
            String accessToken = jwtService.generateAccessToken(authenticatedUser, deviceFingerprint, ipAddress, userAgent);
            String refreshToken = jwtService.generateRefreshToken(authenticatedUser, deviceFingerprint, ipAddress, userAgent);
            
            // Log successful login
            auditService.logSecurityEvent(authenticatedUser.getUsername(), "LOGIN_SUCCESS", ipAddress, userAgent);
            
            return createAuthResponse(accessToken, refreshToken, authenticatedUser, "Login successful");
            
        } catch (BadCredentialsException | LockedException e) {
            handleFailedLogin(request.getUsername(), ipAddress, userAgent);
            throw e;
        }
    }
    
    public AuthResponse register(RegisterRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        
        User savedUser = userRepository.save(user);
        
        logger.info("New user registered: {}", savedUser.getUsername());
        auditService.logSecurityEvent(savedUser.getUsername(), "USER_REGISTERED", null, null);
        
        return createAuthResponse(null, null, savedUser, "User registered successfully");
    }
    
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        try {
            String tokenId = jwtService.extractTokenId(refreshToken);
            String username = jwtService.extractUsername(refreshToken);
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
            
            // Validate refresh token
            if (!jwtService.validateToken(refreshToken, user) || 
                jwtService.extractTokenType(refreshToken) != com.phynance.gateway.model.TokenType.REFRESH) {
                throw new BadCredentialsException("Invalid refresh token");
            }
            
            // Blacklist old refresh token
            jwtService.blacklistToken(tokenId);
            
            // Generate new tokens
            String deviceFingerprint = jwtService.extractDeviceFingerprint(refreshToken);
            String newAccessToken = jwtService.generateAccessToken(user, deviceFingerprint, ipAddress, userAgent);
            String newRefreshToken = jwtService.generateRefreshToken(user, deviceFingerprint, ipAddress, userAgent);
            
            auditService.logSecurityEvent(user.getUsername(), "TOKEN_REFRESHED", ipAddress, userAgent);
            
            return createAuthResponse(newAccessToken, newRefreshToken, user, "Token refreshed successfully");
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid refresh token");
        }
    }
    
    public void logout(String token, String ipAddress, String userAgent) {
        try {
            String tokenId = jwtService.extractTokenId(token);
            String username = jwtService.extractUsername(token);
            
            jwtService.blacklistToken(tokenId);
            
            auditService.logSecurityEvent(username, "LOGOUT", ipAddress, userAgent);
            
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
        }
    }
    
    public void logoutAllDevices(String username, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        jwtService.blacklistAllUserTokens(user);
        
        auditService.logSecurityEvent(username, "LOGOUT_ALL_DEVICES", ipAddress, userAgent);
    }
    
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        
        // Validate new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Blacklist all existing tokens to force re-login
        jwtService.blacklistAllUserTokens(user);
        
        auditService.logSecurityEvent(username, "PASSWORD_CHANGED", null, null);
    }
    
    private void handleFailedLogin(String username, String ipAddress, String userAgent) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            
            if (user.getFailedAttempts() >= maxFailedAttempts) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());
                logger.warn("Account locked for user: {} due to {} failed attempts", username, maxFailedAttempts);
            }
            
            userRepository.save(user);
            auditService.logSecurityEvent(username, "LOGIN_FAILED", ipAddress, userAgent);
        }
    }
    
    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockTime(null);
        userRepository.save(user);
    }
    
    private void unlockAccount(User user) {
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }
    
    private String generateDeviceFingerprint(String ipAddress, String userAgent) {
        // Simple device fingerprinting - in production, use more sophisticated methods
        return ipAddress + "|" + userAgent;
    }
    
    private AuthResponse createAuthResponse(String accessToken, String refreshToken, User user, String message) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(900L); // 15 minutes
        response.setMessage(message);
        
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setFirstName(user.getFirstName());
        userInfo.setLastName(user.getLastName());
        userInfo.setRole(user.getRole());
        userInfo.setLastLogin(user.getLastLogin());
        userInfo.setTwoFactorEnabled(user.isTwoFactorEnabled());
        
        response.setUser(userInfo);
        return response;
    }
} 