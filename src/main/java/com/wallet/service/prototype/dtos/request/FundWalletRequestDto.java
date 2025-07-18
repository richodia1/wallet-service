package com.wallet.service.prototype.dtos.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class FundWalletRequestDto {
    private String customerReference;         // Replaces walletId
    private BigDecimal amount;
    private String narration;
}
