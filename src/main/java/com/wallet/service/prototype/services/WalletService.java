package com.wallet.service.prototype.services;

import com.wallet.service.prototype.dtos.request.CreateWalletRequestDto;
import com.wallet.service.prototype.dtos.request.FundWalletRequestDto;
import com.wallet.service.prototype.dtos.response.LedgerEntryResponseDto;
import com.wallet.service.prototype.dtos.response.WalletTransactionResponse;
import com.wallet.service.prototype.entities.*;
import com.wallet.service.prototype.enums.*;
import com.wallet.service.prototype.models.ResponseModel;
import com.wallet.service.prototype.repositories.*;
import jakarta.persistence.criteria.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletTransactionRepository walletTransactionRepository;


    public ResponseEntity<ResponseModel> getLedgerEntriesByWalletId(UUID walletId) {
        Specification<LedgerEntry> spec = (root, query, cb) -> {
            Order order = cb.desc(root.get("timeStamp"));
            query.orderBy(order);
            return cb.equal(root.get("wallet").get("id"), walletId);
        };

        List<LedgerEntry> ledgerEntries = ledgerEntryRepository.findAll(spec);
        List<LedgerEntryResponseDto> responseList = ledgerEntries.stream()
                .map(LedgerEntryResponseDto::fromEntity)
                .toList();

        return ResponseModel.successResponse(responseList, 200, "Ledger entries fetched successfully");
    }

    public ResponseEntity<ResponseModel> getWalletTransactionHistory(UUID walletId) {
        try {
            Specification<WalletTransaction> spec = (root, query, cb) ->
                    cb.equal(root.get("wallet").get("id"), walletId);

            List<WalletTransaction> transactions = walletTransactionRepository.findAll(
                    spec, Sort.by(Sort.Direction.DESC, "createdAt")
            );
            List<WalletTransactionResponse> responseList = transactions.stream()
                    .map(WalletTransactionResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseModel.successResponse(responseList, HttpStatus.OK.value(), "Wallet transaction history retrieved");
        } catch (Exception ex) {
            return ResponseModel.errorResponse(ex.getMessage(), "Failed to retrieve wallet transaction history", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseModel> createWallet(CreateWalletRequestDto request) {
        Optional<Customer> customerOpt = customerRepository.findByCustomerRef(request.getCustomerReference());
        if (customerOpt.isEmpty()) {
            return ResponseModel.errorResponse("Customer not found", "Invalid reference ID", HttpStatus.NOT_FOUND);
        }

        Customer customer = customerOpt.get();
        String customerRef = customer.getCustomerRef();

        if (walletRepository.existsByCustomerReference(customerRef)) {
            return ResponseModel.errorResponse("Wallet already exists", "Duplicate wallet", HttpStatus.CONFLICT);
        }

        Wallet wallet = new Wallet();
        wallet.setCustomerReference(customerRef);
        wallet.setCurrency(request.getCurrency() != null ? request.getCurrency() : Currency.NGN);
        wallet.setBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO);
        wallet.setActive(request.isActive());

        Wallet savedWallet = walletRepository.save(wallet);
        return ResponseModel.successResponse(savedWallet, HttpStatus.CREATED.value(), "Wallet created successfully");
    }

    public ResponseEntity<ResponseModel> getWalletByCustomerReference(String customerReference) {
        Optional<Wallet> walletOpt = walletRepository.findByCustomerReference(customerReference);

        return walletOpt.map(wallet -> ResponseModel.successResponse(wallet, HttpStatus.OK.value(), "Wallet retrieved successfully")).orElseGet(() -> ResponseModel.errorResponse("Wallet not found", "No wallet for this customer", HttpStatus.NOT_FOUND));

    }
    public ResponseEntity<ResponseModel> fundWallet(FundWalletRequestDto requestDto) {
        try {
            // Find wallet by customerReference
            Wallet wallet = walletRepository.findByCustomerReference(requestDto.getCustomerReference())
                    .orElseThrow(() -> new RuntimeException("Wallet not found for customerReference: " + requestDto.getCustomerReference()));

            BigDecimal previousBalance = wallet.getBalance();
            BigDecimal newBalance = previousBalance.add(requestDto.getAmount());

            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            // Create WalletTransaction record
            WalletTransaction transaction = new WalletTransaction();
            transaction.setCustomerReference(wallet.getCustomerReference());
            transaction.setWallet(wallet);
            transaction.setAmount(requestDto.getAmount());
            transaction.setTransactionType(TransactionType.FUND);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setReference(UUID.randomUUID().toString());
            transaction.setNarration(requestDto.getNarration());

            walletTransactionRepository.save(transaction);

            // Create LedgerEntry
            LedgerEntry ledgerEntry = LedgerEntry.builder()
                    .wallet(wallet)
                    .amount(requestDto.getAmount())
                    .type(LedgerType.CREDIT)
                    .source(TransactionSource.FUND)
                    .reference(transaction.getReference())
                    .previousBalance(previousBalance)
                    .currentBalance(newBalance)
                    .build();

            ledgerEntryRepository.save(ledgerEntry);

            return ResponseModel.successResponse(wallet, HttpStatus.OK.value(), "Wallet funded successfully");
        } catch (Exception ex) {
            return ResponseModel.errorResponse(ex.getMessage(), "Failed to fund wallet", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public UUID getDefaultBankAccountId(String customerReference) {
        return walletRepository.findByCustomerReference(customerReference)
                .map(Wallet::getDefaultBankAccount)
                .map(BankAccount::getId)
                .orElse(null);
    }
    public ResponseEntity<ResponseModel> withdrawFromWalletToBankAccount(FundWalletRequestDto requestDto) {
        try {
            // Fetch wallet by customer reference
            Wallet wallet = walletRepository.findByCustomerReference(requestDto.getCustomerReference())
                    .orElseThrow(() -> new RuntimeException("Wallet not found for customerReference: " + requestDto.getCustomerReference()));

            if (!wallet.isActive()) {
                throw new RuntimeException("Wallet is not active.");
            }

            if (wallet.getDefaultBankAccount() == null) {
                throw new RuntimeException("No default bank account linked to the wallet.");
            }

            BigDecimal currentBalance = wallet.getBalance();
            BigDecimal withdrawalAmount = requestDto.getAmount();

            if (currentBalance.compareTo(withdrawalAmount) < 0) {
                throw new RuntimeException("Insufficient wallet balance.");
            }

            // Debit the wallet
            BigDecimal newBalance = currentBalance.subtract(withdrawalAmount);
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            // Create transaction
            WalletTransaction transaction = new WalletTransaction();
            transaction.setCustomerReference(wallet.getCustomerReference());
            transaction.setWallet(wallet);
            transaction.setAmount(withdrawalAmount);
            transaction.setTransactionType(TransactionType.WITHDRAW);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setReference(UUID.randomUUID().toString());
            transaction.setNarration(requestDto.getNarration());

            walletTransactionRepository.save(transaction);

            // Create ledger entry
            LedgerEntry ledgerEntry = LedgerEntry.builder()
                    .wallet(wallet)
                    .amount(withdrawalAmount)
                    .type(LedgerType.DEBIT)
                    .source(TransactionSource.WITHDRAW)
                    .reference(transaction.getReference())
                    .previousBalance(currentBalance)
                    .currentBalance(newBalance)
                    .build();

            ledgerEntryRepository.save(ledgerEntry);

            return ResponseModel.successResponse(wallet, HttpStatus.OK.value(), "Withdrawal successful to bank account");

        } catch (Exception e) {
            return ResponseModel.errorResponse(e.getMessage(), "Withdrawal failed", HttpStatus.BAD_REQUEST);
        }
    }


    public ResponseEntity<ResponseModel> getDefaultBankAccountIdResponse(String customerReference) {
        UUID bankAccountId = getDefaultBankAccountId(customerReference);

        if (bankAccountId == null) {
            return ResponseModel.errorResponse("Not Found", "No default bank account associated", HttpStatus.NOT_FOUND);
        }

        return ResponseModel.successResponse(bankAccountId, HttpStatus.OK.value(), "Default bank account ID retrieved successfully");
    }

}
