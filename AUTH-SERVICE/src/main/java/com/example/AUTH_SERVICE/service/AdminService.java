package com.example.AUTH_SERVICE.service;

import java.util.List;

import com.example.AUTH_SERVICE.dto.UserResponse;

public interface AdminService {

    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    void deleteUser(Long id);
    UserResponse toggleUserStatus(Long id);


}
