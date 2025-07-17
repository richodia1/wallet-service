package com.wallet.service.prototype.dtos.request;

import lombok.Data;

import java.util.UUID;

@Data
public class LinkBankAccountRequestDto {
    private String customerReference;
    private UUID bankAccountId;
}