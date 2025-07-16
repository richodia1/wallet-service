package com.wallet.service.prototype.dtos.response;

import com.wallet.service.prototype.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class LedgerEntryResponse {
    private Long id;
    private String walletId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal currentBalance;
    private String narration;
    private Instant timestamp;
}
