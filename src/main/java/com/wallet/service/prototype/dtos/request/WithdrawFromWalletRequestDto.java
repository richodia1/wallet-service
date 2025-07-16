package com.wallet.service.prototype.dtos.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WithdrawFromWalletRequestDto {
    private UUID walletId;
    private Long bankAccountId;
    private BigDecimal amount;
    private String narration;
}
