package com.example.Order.Service.config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
@Slf4j
public class FeignClientConfig {
    
    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return template -> {
            try {
                // ✅ FIX: Use SecurityContextHolder instead of RequestContextHolder
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication instanceof JwtAuthenticationToken) {
                    Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
                    String token = jwt.getTokenValue();
                    
                    template.header("Authorization", "Bearer " + token);
                    log.debug("✅ Added JWT token from SecurityContext to Feign request");
                } else {
                    log.warn("⚠️ No JWT token found in SecurityContext, authentication class: {}", 
                        authentication != null ? authentication.getClass().getSimpleName() : "null");
                    
                    // Try to get from RequestContextHolder as fallback
                    try {
                        jakarta.servlet.http.HttpServletRequest request = 
                            ((org.springframework.web.context.request.ServletRequestAttributes) 
                            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                            .getRequest();
                        String authHeader = request.getHeader("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            template.header("Authorization", authHeader);
                            log.debug("✅ Found token in RequestContextHolder as fallback");
                        }
                    } catch (Exception e) {
                        log.debug("No token in RequestContextHolder either");
                    }
                }
            } catch (Exception e) {
                log.error("❌ Error adding token to Feign request: {}", e.getMessage());
            }
        };
    }
}