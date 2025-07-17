package com.wallet.service.prototype.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class WalletResponseDto {
    private UUID id;
    private String customerReference;
    private BigDecimal balance;
    private boolean active;
    private String defaultBankAccountNumber;
}
