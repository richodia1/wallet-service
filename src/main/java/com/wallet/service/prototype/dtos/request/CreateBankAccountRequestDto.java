package com.wallet.service.prototype.dtos.request;

import lombok.Data;

@Data
public class CreateBankAccountRequestDto {
    private Long customerId;
    private String accountNumber;
    private String accountName;
    private String bankName;
    private String bankCode;
}
