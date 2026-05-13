package com.example.AUTH_SERVICE.service;



import com.example.AUTH_SERVICE.dto.AddressRequest;
import com.example.AUTH_SERVICE.dto.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getUserAddresses(Long userId);
    AddressResponse addAddress(Long userId, AddressRequest request);
    AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);
    void deleteAddress(Long userId, Long addressId);
    void setDefaultAddress(Long userId, Long addressId);
}