package com.wallet.service.prototype.services;

import com.wallet.service.prototype.dtos.request.CreateCustomerRequestDto;
import com.wallet.service.prototype.dtos.response.CustomerResponseDto;
import com.wallet.service.prototype.entities.Customer;
import com.wallet.service.prototype.models.ResponseModel;
import com.wallet.service.prototype.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public ResponseEntity<ResponseModel> createCustomer(CreateCustomerRequestDto requestDto) {
        String ref = requestDto.getCustomerRef() != null && !requestDto.getCustomerRef().isBlank()
                ? requestDto.getCustomerRef()
                : "CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Customer customer = Customer.builder()
                .customerRef(ref)
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .middleName(requestDto.getMiddleName())
                .email(requestDto.getEmail())
                .phoneNumber(requestDto.getPhoneNumber())
                .dateOfBirth(requestDto.getDateOfBirth())
                .gender(requestDto.getGender())
                .bvn(requestDto.getBvn())
                .nin(requestDto.getNin())
                .residentialAddress(requestDto.getResidentialAddress())
                .stateOfOrigin(requestDto.getStateOfOrigin())
                .lgaOfOrigin(requestDto.getLgaOfOrigin())
                .nationality(requestDto.getNationality())
                .idType(requestDto.getIdType())
                .idNumber(requestDto.getIdNumber())
                .idIssueDate(requestDto.getIdIssueDate())
                .idExpiryDate(requestDto.getIdExpiryDate())
                .status(requestDto.getStatus() != null ? requestDto.getStatus() : "ACTIVE")
                .build();

        Customer saved = customerRepository.save(customer);

        CustomerResponseDto responseDto = CustomerResponseDto.builder()
                .customerRef(saved.getCustomerRef())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .phoneNumber(saved.getPhoneNumber())
                .status(saved.getStatus())
                .build();
        return ResponseModel.successResponse(responseDto, HttpStatus.OK.value(), "Customer created successfully");

    }
}
