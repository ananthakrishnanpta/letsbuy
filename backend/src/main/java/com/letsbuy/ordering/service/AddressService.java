package com.letsbuy.ordering.service;

import com.letsbuy.common.exception.NotFoundException;
import com.letsbuy.ordering.domain.Address;
import com.letsbuy.ordering.persistence.AddressRepository;
import com.letsbuy.ordering.web.dto.AddressRequest;
import com.letsbuy.ordering.web.dto.AddressResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private final AddressRepository addresses;

    public AddressService(AddressRepository addresses) {
        this.addresses = addresses;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID userId) {
        return addresses.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse create(UUID userId, AddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        apply(address, request);
        if (request.defaultAddress()) {
            clearDefault(userId);
        }
        if (addresses.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId).isEmpty()) {
            address.setDefaultAddress(true);
        }
        return AddressResponse.from(addresses.save(address));
    }

    private void apply(Address address, AddressRequest request) {
        address.setFullName(request.fullName().trim());
        address.setLine1(request.line1().trim());
        address.setLine2(blank(request.line2()));
        address.setCity(request.city().trim());
        address.setRegion(request.region().trim());
        address.setPostalCode(request.postalCode().trim());
        address.setCountryCode(request.countryCode().trim().toUpperCase(java.util.Locale.ROOT));
        address.setPhone(request.phone().trim());
        address.setDefaultAddress(request.defaultAddress());
    }

    private void clearDefault(UUID userId) {
        for (Address address : addresses.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId)) {
            address.setDefaultAddress(false);
        }
    }

    Address requireOwned(UUID userId, UUID addressId) {
        return addresses.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new NotFoundException("Address not found."));
    }

    private static String blank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
