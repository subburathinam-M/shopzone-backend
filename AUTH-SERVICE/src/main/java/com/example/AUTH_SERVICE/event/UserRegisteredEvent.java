package com.example.AUTH_SERVICE.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private Long userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime timestamp;
}