package com.wallet.service.prototype.repositories;

import com.wallet.service.prototype.entities.BankAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;


public interface BankAccountRepository extends JpaRepository<BankAccount, UUID>, JpaSpecificationExecutor<BankAccount> {
  boolean existsByAccountNumberAndBankName(String accountNumber, String bankName);
  List<BankAccount> findByWalletId(UUID walletId);
}
