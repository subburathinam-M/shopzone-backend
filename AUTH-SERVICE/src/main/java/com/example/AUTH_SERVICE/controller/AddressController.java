package com.example.AUTH_SERVICE.controller;

import com.example.AUTH_SERVICE.dto.AddressRequest;
import com.example.AUTH_SERVICE.dto.AddressResponse;
import com.example.AUTH_SERVICE.entity.User;
import com.example.AUTH_SERVICE.repository.UserRepository;
import com.example.AUTH_SERVICE.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails.getUsername());
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressRequest request) {
        Long userId = getUserId(userDetails.getUsername());
        return ResponseEntity.ok(addressService.addAddress(userId, request));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId,
            @RequestBody AddressRequest request) {
        Long userId = getUserId(userDetails.getUsername());
        return ResponseEntity.ok(addressService.updateAddress(userId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {
        Long userId = getUserId(userDetails.getUsername());
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {
        Long userId = getUserId(userDetails.getUsername());
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    private Long getUserId(String username) {
        return userRepository.findByUsernameOrEmail(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
