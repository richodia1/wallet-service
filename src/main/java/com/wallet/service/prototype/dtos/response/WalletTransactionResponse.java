package com.wallet.service.prototype.dtos.response;

import com.wallet.service.prototype.entities.WalletTransaction;
import com.wallet.service.prototype.enums.TransactionStatus;
import com.wallet.service.prototype.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class WalletTransactionResponse {

    private UUID id;
    private String customerReference;
    private UUID walletId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String reference;
    private Instant createdAt;
    private String narration;

    public static WalletTransactionResponse fromEntity(WalletTransaction transaction) {
        return WalletTransactionResponse.builder()
                .id(transaction.getId())
                .customerReference(transaction.getCustomerReference())
                .walletId(transaction.getWallet().getId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .reference(transaction.getReference())
                .createdAt(transaction.getCreatedAt())
                .narration(transaction.getNarration())
                .build();
    }
}
