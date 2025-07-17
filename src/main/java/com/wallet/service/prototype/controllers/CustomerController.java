package com.wallet.service.prototype.controllers;

import com.wallet.service.prototype.dtos.request.CreateCustomerRequestDto;
import com.wallet.service.prototype.models.ResponseModel;
import com.wallet.service.prototype.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/add-customer")
    public ResponseEntity<ResponseModel> createCustomer(@Valid @RequestBody CreateCustomerRequestDto requestDto) {
        return customerService.createCustomer(requestDto);
    }
}