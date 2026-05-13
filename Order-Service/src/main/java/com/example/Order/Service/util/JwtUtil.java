// package com.example.Order.Service.util;



// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;

// import jakarta.servlet.http.HttpServletRequest;

// @Component
// @Slf4j
// public class JwtUtil {

//     @Value("${jwt.secret}")
//     private String jwtSecret;

//     // 🔥 FIXED: First check SecurityContext, then fallback to token extraction
//     public UserInfo getCurrentUser() {
//         try {
//             // First try to get from SecurityContext (set by filter)
//             var authentication = SecurityContextHolder.getContext().getAuthentication();
//             if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
//                 UserInfo userInfo = (UserInfo) authentication.getPrincipal();
//                 log.info("Got user from SecurityContext: {}", userInfo);
//                 return userInfo;
//             }
            
//             // Fallback to token extraction from request
//             String token = extractTokenFromRequest();
//             return extractUserInfoFromToken(token);
//         } catch (Exception e) {
//             log.error("Failed to get current user: {}", e.getMessage());
//             throw new RuntimeException("Could not identify user: " + e.getMessage());
//         }
//     }

//     private String extractTokenFromRequest() {
//         ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//         if (attributes == null) {
//             throw new RuntimeException("No request context found");
//         }
        
//         HttpServletRequest request = attributes.getRequest();
//         String authHeader = request.getHeader("Authorization");
        
//         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//             throw new RuntimeException("No valid token found");
//         }
        
//         return authHeader.substring(7);
//     }

//     private UserInfo extractUserInfoFromToken(String token) {
//         Claims claims = Jwts.parser()
//                 .setSigningKey(jwtSecret)
//                 .parseClaimsJws(token)
//                 .getBody();
        
//         Long userId = claims.get("id", Long.class);
//         String username = claims.getSubject();
//         String email = claims.get("email", String.class);
//         String role = claims.get("role", String.class);
        
//         return new UserInfo(userId, username, email, role);
//     }

//     public UserInfo validateTokenAndGetUser(String token) {
//         try {
//             Claims claims = Jwts.parser()
//                     .setSigningKey(jwtSecret)
//                     .parseClaimsJws(token)
//                     .getBody();
            
//             Long userId = claims.get("id", Long.class);
//             String username = claims.getSubject();
//             String email = claims.get("email", String.class);
//             String role = claims.get("role", String.class);
            
//             log.info("Token validated for user: {} (ID: {})", username, userId);
            
//             return new UserInfo(userId, username, email, role);
//         } catch (Exception e) {
//             log.error("Token validation failed: {}", e.getMessage());
//             throw new RuntimeException("Invalid token");
//         }
//     }

//     public boolean validateToken(String token) {
//         try {
//             Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
//             return true;
//         } catch (Exception e) {
//             log.error("Invalid token: {}", e.getMessage());
//             return false;
//         }
//     }

//     public static class UserInfo {
//         private final Long userId;
//         private final String username;
//         private final String email;
//         private final String role;

//         public UserInfo(Long userId, String username, String email, String role) {
//             this.userId = userId;
//             this.username = username;
//             this.email = email;
//             this.role = role;
//         }

//         public Long getUserId() { return userId; }
//         public String getUsername() { return username; }
//         public String getEmail() { return email; }
//         public String getRole() { return role; }
        
//         @Override
//         public String toString() {
//             return String.format("UserInfo{id=%d, username='%s', email='%s', role='%s'}", 
//                     userId, username, email, role);
//         }
//     }
// }