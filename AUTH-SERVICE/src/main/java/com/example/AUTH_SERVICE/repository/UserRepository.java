package com.example.AUTH_SERVICE.repository;

import com.example.AUTH_SERVICE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    Optional<User> findByKeycloakId(String keycloakId);  // 👈 NEW
    // Find user by username OR email for login
    @Query("SELECT u FROM User u WHERE u.username = :login OR u.email = :login")
    Optional<User> findByUsernameOrEmail(@Param("login") String login);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByResetToken(String resetToken);  // 🔥 NEW
}