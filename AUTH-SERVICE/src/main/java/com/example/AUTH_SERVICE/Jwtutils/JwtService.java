// package com.example.AUTH_SERVICE.Jwtutils;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.io.Decoders;
// import io.jsonwebtoken.security.Keys;
// import lombok.extern.slf4j.Slf4j;  // ✅ ADD THIS
// import org.springframework.beans.factory.annotation.Value;
// import com.example.AUTH_SERVICE.entity.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Service;

// import com.example.AUTH_SERVICE.security.CustomUserDetails;

// import java.security.Key;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Function;

// @Service
// @Slf4j  // ✅ ADD THIS for log
// public class JwtService {

//     @Value("${jwt.secret}")
//     private String secretKey;

//     @Value("${jwt.expiration}")
//     private long jwtExpiration;

//     @Value("${jwt.refresh-expiration}")
//     private long refreshExpiration;

//     public String extractUsername(String token) {
//         return extractClaim(token, Claims::getSubject);
//     }

//     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//         final Claims claims = extractAllClaims(token);
//         return claimsResolver.apply(claims);
//     }

//     // 🔥 FIXED: extractUserId method - uses class field secretKey
//     public Long extractUserId(String token) {
//         try {
//             Claims claims = Jwts.parser()
//                     .setSigningKey(getSignInKey())  // ✅ Uses class method
//                     .parseClaimsJws(token)
//                     .getBody();
//             return claims.get("id", Long.class);
//         } catch (Exception e) {
//             log.error("Failed to extract user ID from token: {}", e.getMessage());  // ✅ Now works
//             throw new RuntimeException("Invalid token");
//         }
//     }

//     public String generateToken(UserDetails userDetails) {
//         Map<String, Object> claims = new HashMap<>();
        
//         if (userDetails instanceof CustomUserDetails) {
//             CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
//             User user = customUserDetails.getUser();
//             claims.put("id", user.getId());
//             claims.put("email", user.getEmail());
//             claims.put("role", user.getRole());
//         }
        
//         return generateToken(claims, userDetails);
//     }

//     public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//         return buildToken(extraClaims, userDetails, jwtExpiration);
//     }

//     public String generateRefreshToken(UserDetails userDetails) {
//         return buildToken(new HashMap<>(), userDetails, refreshExpiration);
//     }

//     private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
//         return Jwts
//                 .builder()
//                 .setClaims(extraClaims)
//                 .setSubject(userDetails.getUsername())
//                 .setIssuedAt(new Date(System.currentTimeMillis()))
//                 .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                 .signWith(getSignInKey(), SignatureAlgorithm.HS256)
//                 .compact();
//     }

//     public boolean isTokenValid(String token, UserDetails userDetails) {
//         final String username = extractUsername(token);
//         return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//     }

//     private boolean isTokenExpired(String token) {
//         return extractExpiration(token).before(new Date());
//     }

//     private Date extractExpiration(String token) {
//         return extractClaim(token, Claims::getExpiration);
//     }

//     private Claims extractAllClaims(String token) {
//         return Jwts
//                 .parserBuilder()
//                 .setSigningKey(getSignInKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     private Key getSignInKey() {
//         byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//         return Keys.hmacShaKeyFor(keyBytes);
//     }
// }