package com.wallet.service.prototype.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponseDto {
    private String customerRef;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String status; // ACTIVE, INACTIVE, etc.
}
