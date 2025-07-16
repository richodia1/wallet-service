package com.wallet.service.prototype.dtos.request;

import com.wallet.service.prototype.enums.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWalletRequestDto {
    private String customerReference; // Matches Wallet entity field
    private BigDecimal initialBalance = BigDecimal.ZERO; // Optional, defaults to 0.00
    private Currency currency;
    private boolean active = true;
}
