package com.wallet.service.prototype.entities;
import com.wallet.service.prototype.enums.LedgerType;
import com.wallet.service.prototype.enums.TransactionSource;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 38, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerType type; // CREDIT or DEBIT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSource source; // FUND, WITHDRAW, TRANSFER, etc.

    @Column(name = "previous_balance", nullable = false, precision = 38, scale = 2)
    private BigDecimal previousBalance;

    @Column(name = "current_balance", nullable = false, precision = 38, scale = 2)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private String reference;

    @Column(nullable = false, updatable = false)
    private Instant timeStamp = Instant.now();
}