package com.letsbuy.ordering.web.dto;

import com.letsbuy.ordering.domain.Address;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String fullName,
        String line1,
        String line2,
        String city,
        String region,
        String postalCode,
        String countryCode,
        String phone,
        boolean defaultAddress
) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.getPhone(),
                address.isDefaultAddress()
        );
    }
}
