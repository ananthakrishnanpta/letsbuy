package com.letsbuy.ordering.web;

import com.letsbuy.common.api.ApiResponse;
import com.letsbuy.ordering.service.AddressService;
import com.letsbuy.ordering.web.dto.AddressRequest;
import com.letsbuy.ordering.web.dto.AddressResponse;
import com.letsbuy.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addresses;

    public AddressController(AddressService addresses) {
        this.addresses = addresses;
    }

    @GetMapping
    ApiResponse<List<AddressResponse>> list() {
        return ApiResponse.of(addresses.list(CurrentUser.id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.of(addresses.create(CurrentUser.id(), request));
    }
}
