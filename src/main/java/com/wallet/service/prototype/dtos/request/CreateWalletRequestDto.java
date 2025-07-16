package com.wallet.service.prototype.dtos.request;

import com.wallet.service.prototype.enums.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWalletRequestDto {
    private Long customerId; // References the Customer entity
    private BigDecimal initialBalance; // Optional - can be 0.00
    private Currency currency;
    private boolean active = true;
}
