package com.letsbuy.ordering.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Size(max = 160) String line1,
        @Size(max = 160) String line2,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 80) String region,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Size(min = 2, max = 2) String countryCode,
        @NotBlank @Size(max = 32) String phone,
        boolean defaultAddress
) {
}
