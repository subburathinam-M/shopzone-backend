// package com.example.Order.Service.config;


// import feign.RequestInterceptor;
// import feign.RequestTemplate;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;

// @Configuration
// @Slf4j
// public class TokenRelayConfig {

//     @Bean
//     public RequestInterceptor tokenRelayRequestInterceptor() {
//         return new RequestInterceptor() {
//             @Override
//             public void apply(RequestTemplate template) {
//                 relayJwtToken(template);
//             }
            
//             private void relayJwtToken(RequestTemplate template) {
//                 // Method 1: Try SecurityContext (works for @Async calls)
//                 try {
//                     var authentication = SecurityContextHolder.getContext().getAuthentication();
//                     if (authentication instanceof JwtAuthenticationToken) {
//                         Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
//                         String tokenValue = jwt.getTokenValue();
//                         template.header("Authorization", "Bearer " + tokenValue);
//                         log.debug("🔐 Token relayed via SecurityContext");
//                         return;
//                     }
//                 } catch (Exception e) {
//                     log.debug("Could not get token from SecurityContext: {}", e.getMessage());
//                 }
                
//                 // Method 2: Try RequestContextHolder (works for synchronous calls)
//                 try {
//                     ServletRequestAttributes attributes = 
//                         (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//                     if (attributes != null) {
//                         String authHeader = attributes.getRequest().getHeader("Authorization");
//                         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                             template.header("Authorization", authHeader);
//                             log.debug("🔐 Token relayed via RequestContextHolder");
//                             return;
//                         }
//                     }
//                 } catch (Exception e) {
//                     log.debug("Could not get token from RequestContextHolder: {}", e.getMessage());
//                 }
                
//                 log.warn("⚠️ No JWT token found to relay to downstream service");
//             }
//         };
//     }
// }