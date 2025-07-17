package com.wallet.service.prototype.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LinkBankAccountResponseDto {
    private UUID walletId;
    private String customerReference;
    private UUID linkedBankAccountId;
    private String bankName;
    private String accountNumber;
}