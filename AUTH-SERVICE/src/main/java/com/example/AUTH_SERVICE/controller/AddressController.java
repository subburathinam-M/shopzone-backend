package com.example.AUTH_SERVICE.controller;

import com.example.AUTH_SERVICE.dto.AddressRequest;
import com.example.AUTH_SERVICE.dto.AddressResponse;
import com.example.AUTH_SERVICE.service.AddressService;
import lombok.RequiredArgsConstructor;
import com.example.AUTH_SERVICE.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.AUTH_SERVICE.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
       
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        // You need to get local user ID from keycloakId
        Long userId = getLocalUserIdFromKeycloakId(keycloakId);
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AddressRequest request) {
        String keycloakId = jwt.getSubject();
        Long userId = getLocalUserIdFromKeycloakId(keycloakId);
        return ResponseEntity.ok(addressService.addAddress(userId, request));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long addressId,
            @RequestBody AddressRequest request) {
        String keycloakId = jwt.getSubject();
        Long userId = getLocalUserIdFromKeycloakId(keycloakId);
        return ResponseEntity.ok(addressService.updateAddress(userId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long addressId) {
        String keycloakId = jwt.getSubject();
        Long userId = getLocalUserIdFromKeycloakId(keycloakId);
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long addressId) {
        String keycloakId = jwt.getSubject();
        Long userId = getLocalUserIdFromKeycloakId(keycloakId);
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    private Long getLocalUserIdFromKeycloakId(String keycloakId) {
        // You need to inject UserRepository and fetch local ID
        // This is a helper method - implement properly
        return userRepository.findByKeycloakId(keycloakId)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}