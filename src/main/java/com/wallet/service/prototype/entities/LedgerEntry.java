package com.wallet.service.prototype.entities;

import com.wallet.service.prototype.enums.LedgerType;
import com.wallet.service.prototype.enums.TransactionSource;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
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
    private LedgerType type; // DEBIT or CREDIT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSource source; // e.g., FUND, WITHDRAW, TRANSFER
    @Column(name = "previous_balance", nullable = false, precision = 38, scale = 2)
    private BigDecimal previousBalance;

    @Column(name = "current_balance", nullable = false, precision = 38, scale = 2)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private String reference; // Could be a transaction ID

    @Column(nullable = false)
    private Instant timeStamp = Instant.now();
}