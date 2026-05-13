package com.example.AUTH_SERVICE.service.serviceImpl;


import com.example.AUTH_SERVICE.dto.AddressRequest;
import com.example.AUTH_SERVICE.dto.AddressResponse;
import com.example.AUTH_SERVICE.entity.Address;
import com.example.AUTH_SERVICE.entity.User;
import com.example.AUTH_SERVICE.repository.AddressRepository;
import com.example.AUTH_SERVICE.repository.UserRepository;
import com.example.AUTH_SERVICE.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressResponse> getUserAddresses(Long userId) {
        log.info("Fetching addresses for user: {}", userId);
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        log.info("Adding address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If this is default address, reset other defaults
        if (request.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = new Address();
        address.setUser(user);
        address.setAddressType(request.getAddressType());
        address.setName(request.getName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault());

        Address savedAddress = addressRepository.save(address);
        log.info("Address added with ID: {}", savedAddress.getId());

        return mapToResponse(savedAddress);
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this address");
        }

        // If this is default address, reset other defaults
        if (request.getIsDefault() && !address.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        address.setAddressType(request.getAddressType());
        address.setName(request.getName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault());

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated with ID: {}", updatedAddress.getId());

        return mapToResponse(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this address");
        }

        addressRepository.delete(address);
        log.info("Address deleted with ID: {}", addressId);
    }

    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        log.info("Setting address {} as default for user: {}", addressId, userId);

        // Reset all addresses to non-default
        addressRepository.findByUserId(userId).forEach(addr -> {
            addr.setIsDefault(false);
            addressRepository.save(addr);
        });

        // Set selected address as default
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to set this address as default");
        }

        address.setIsDefault(true);
        addressRepository.save(address);
        log.info("Address {} set as default", addressId);
    }

    private AddressResponse mapToResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setAddressType(address.getAddressType());
        response.setName(address.getName());
        response.setPhoneNumber(address.getPhoneNumber());
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setLandmark(address.getLandmark());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPincode(address.getPincode());
        response.setCountry(address.getCountry());
        response.setIsDefault(address.getIsDefault());
        response.setCreatedAt(address.getCreatedAt());
        return response;
    }
}