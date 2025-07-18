package com.wallet.service.prototype.repositories;

import com.wallet.service.prototype.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID>, JpaSpecificationExecutor<Wallet> {
    Optional<Wallet> findByCustomerReference(String customerReference);

    boolean existsByCustomerReference(String customerReference);

}
