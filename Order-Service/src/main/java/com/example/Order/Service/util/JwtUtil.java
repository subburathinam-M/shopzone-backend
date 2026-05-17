package com.example.Order.Service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public UserInfo getCurrentUser(String token) {
        return extractUserInfoFromToken(token);
    }

    private UserInfo extractUserInfoFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = claims.get("id", Long.class);
        String username = claims.getSubject();
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);

        return new UserInfo(userId, username, email, role);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public static class UserInfo {
        private final Long userId;
        private final String username;
        private final String email;
        private final String role;

        public UserInfo(Long userId, String username, String email, String role) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }

        @Override
        public String toString() {
            return String.format("UserInfo{id=%d, username='%s', email='%s', role='%s'}",
                    userId, username, email, role);
        }
    }
}
