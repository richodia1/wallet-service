package com.wallet.service.prototype.dtos.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateCustomerRequestDto {
    private Long id;
    private String customerRef;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;               // M, F, O
    private String bvn;
    private String nin;
    private String residentialAddress;
    private String stateOfOrigin;
    private String lgaOfOrigin;
    private String nationality;
    private String idType;              // e.g., NIN, Driver’s License
    private String idNumber;
    private LocalDate idIssueDate;
    private LocalDate idExpiryDate;
    private String status;
}
