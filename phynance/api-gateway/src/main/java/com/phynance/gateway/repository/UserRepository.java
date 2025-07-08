package com.phynance.gateway.repository;

import com.phynance.gateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.failedAttempts >= :maxAttempts AND u.lockTime IS NOT NULL AND u.lockTime < :unlockTime")
    List<User> findLockedUsers(@Param("maxAttempts") int maxAttempts, @Param("unlockTime") LocalDateTime unlockTime);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :inactiveTime")
    List<User> findInactiveUsers(@Param("inactiveTime") LocalDateTime inactiveTime);
    
    List<User> findByRole(com.phynance.gateway.model.UserRole role);
} 