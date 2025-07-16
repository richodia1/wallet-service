package com.wallet.service.prototype.repositories;


import com.wallet.service.prototype.entities.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID>, JpaSpecificationExecutor<WalletTransaction> {
}