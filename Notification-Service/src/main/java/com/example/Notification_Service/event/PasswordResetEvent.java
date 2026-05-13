package com.example.Notification_Service.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetEvent {
    private Long userId;
    private String email;
    private String username;
    private String resetToken;
    private LocalDateTime timestamp;
}