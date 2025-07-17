package com.wallet.service.prototype.dtos.request;

import lombok.Data;

@Data
public class CreateBankAccountRequestDto {
    private String customerReference;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private String accountType; // "Savings" or "Current"
    private String currency;
}
