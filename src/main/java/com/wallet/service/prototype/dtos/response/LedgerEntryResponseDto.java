package com.wallet.service.prototype.dtos.response;

import com.wallet.service.prototype.entities.LedgerEntry;
import com.wallet.service.prototype.enums.LedgerType;
import com.wallet.service.prototype.enums.TransactionSource;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LedgerEntryResponseDto {
    private UUID id;
    private UUID walletId;
    private BigDecimal amount;
    private LedgerType type;
    private TransactionSource source;
    private BigDecimal previousBalance;
    private BigDecimal currentBalance;
    private String reference;
    private Instant timeStamp;

    public static LedgerEntryResponseDto fromEntity(LedgerEntry entry) {
        return LedgerEntryResponseDto.builder()
                .id(entry.getId())
                .walletId(entry.getWallet().getId())
                .amount(entry.getAmount())
                .type(entry.getType())
                .source(entry.getSource())
                .previousBalance(entry.getPreviousBalance())
                .currentBalance(entry.getCurrentBalance())
                .reference(entry.getReference())
                .timeStamp(entry.getTimeStamp())
                .build();
    }
}