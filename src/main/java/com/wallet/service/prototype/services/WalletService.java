package com.wallet.service.prototype.services;

import com.wallet.service.prototype.dtos.request.CreateBankAccountRequestDto;
import com.wallet.service.prototype.dtos.request.CreateWalletRequestDto;
import com.wallet.service.prototype.dtos.request.FundWalletRequestDto;
import com.wallet.service.prototype.dtos.request.LinkBankAccountRequestDto;
import com.wallet.service.prototype.dtos.response.LedgerEntryResponseDto;
import com.wallet.service.prototype.dtos.response.LinkBankAccountResponseDto;
import com.wallet.service.prototype.dtos.response.WalletResponseDto;
import com.wallet.service.prototype.dtos.response.WalletTransactionResponse;
import com.wallet.service.prototype.entities.*;
import com.wallet.service.prototype.enums.*;
import com.wallet.service.prototype.enums.Currency;
import com.wallet.service.prototype.models.ResponseModel;
import com.wallet.service.prototype.repositories.*;
import jakarta.persistence.criteria.Order;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final BankAccountRepository bankAccountRepository;


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
@Transactional
    public ResponseEntity<ResponseModel> createWallet(CreateWalletRequestDto request) {
        Optional<Customer> customerOpt = customerRepository.findByCustomerRef(request.getCustomerReference());

        if (customerOpt.isEmpty()) {
            return ResponseModel.errorResponse("Customer not found", "Invalid reference ID", HttpStatus.NOT_FOUND);
        }

        Customer customer = customerOpt.get();
        log.info("Customer found: {}", customer.getCustomerRef());

        String customerRef = customer.getCustomerRef();

        if (walletRepository.existsByCustomerReference(customerRef)) {
            return ResponseModel.errorResponse("Wallet already exists", "Duplicate wallet", HttpStatus.CONFLICT);
        }

        log.info("Creating wallet for customerRef: {}", customerRef);

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

        return walletOpt.map(wallet -> {
            WalletResponseDto dto = WalletResponseDto.builder()
                    .id(wallet.getId())
                    .customerReference(wallet.getCustomerReference())
                    .balance(wallet.getBalance())
                    .active(wallet.isActive())
                    .defaultBankAccountNumber(
                            wallet.getDefaultBankAccount() != null
                                    ? wallet.getDefaultBankAccount().getAccountNumber()
                                    : null)
                    .build();

            return ResponseModel.successResponse(dto, HttpStatus.OK.value(), "Wallet retrieved successfully");

        }).orElseGet(() ->
                ResponseModel.errorResponse("Wallet not found", "No wallet for this customer", HttpStatus.NOT_FOUND)
        );
    }

    public ResponseEntity<ResponseModel> fundWallet(FundWalletRequestDto requestDto) {
        log.info("Initiating wallet funding for customerReference: {}", requestDto.getCustomerReference());

        try {
            Wallet wallet = walletRepository.findByCustomerReference(requestDto.getCustomerReference())
                    .orElseThrow(() -> {
                        String message = "Wallet not found for customerReference: " + requestDto.getCustomerReference();
                        log.warn(message);
                        return new RuntimeException(message);
                    });

            BigDecimal previousBalance = wallet.getBalance();
            BigDecimal newBalance = previousBalance.add(requestDto.getAmount());

            wallet.setBalance(newBalance);
            walletRepository.save(wallet);
            log.info("Wallet balance updated from {} to {} for customerReference: {}", previousBalance, newBalance, wallet.getCustomerReference());

            WalletTransaction transaction = new WalletTransaction();
            transaction.setCustomerReference(wallet.getCustomerReference());
            transaction.setWallet(wallet);
            transaction.setAmount(requestDto.getAmount());
            transaction.setTransactionType(TransactionType.FUND);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setReference(UUID.randomUUID().toString());
            transaction.setNarration(requestDto.getNarration());

            walletTransactionRepository.save(transaction);
            log.info("WalletTransaction created with reference: {}", transaction.getReference());

            LedgerEntry ledgerEntry = LedgerEntry.builder()
                    .wallet(wallet)
                    .amount(requestDto.getAmount())
                    .type(LedgerType.CREDIT)
                    .source(TransactionSource.FUND)
                    .reference(transaction.getReference())
                    .previousBalance(previousBalance)
                    .currentBalance(newBalance)
                    .timeStamp(Instant.now())
                    .build();

            ledgerEntryRepository.save(ledgerEntry);
            log.info("LedgerEntry recorded for wallet funding. Reference: {}", transaction.getReference());

            return ResponseModel.successResponse(wallet, HttpStatus.OK.value(), "Wallet funded successfully");

        } catch (Exception ex) {
            log.error("Error occurred while funding wallet for customerReference: {}", requestDto.getCustomerReference(), ex);
            return ResponseModel.errorResponse(ex.getMessage(), "Failed to fund wallet", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public ResponseEntity<ResponseModel> createBankAccount(CreateBankAccountRequestDto dto) {
        Wallet wallet = walletRepository.findByCustomerReference(dto.getCustomerReference())
                .orElseThrow(() -> new RuntimeException("Wallet not found for customerReference: " + dto.getCustomerReference()));

        BankAccount account = BankAccount.builder()
                .customerReference(dto.getCustomerReference())
                .bankName(dto.getBankName())
                .accountName(dto.getAccountName())
                .accountNumber(dto.getAccountNumber())
                .accountType(dto.getAccountType())
                .currency(dto.getCurrency())
                .active(true)
                .wallet(wallet)
                .build();

        bankAccountRepository.save(account);

        return ResponseModel.successResponse(account, HttpStatus.CREATED.value(), "Bank account created successfully");
    }

    public ResponseEntity<ResponseModel> linkBankAccountToWallet(LinkBankAccountRequestDto request) {
        try {
            Wallet wallet = walletRepository.findByCustomerReference(request.getCustomerReference())
                    .orElseThrow(() -> new RuntimeException("Wallet not found for customerReference: " + request.getCustomerReference()));

            BankAccount bankAccount = bankAccountRepository.findById(request.getBankAccountId())
                    .orElseThrow(() -> new RuntimeException("Bank account not found with ID: " + request.getBankAccountId()));

            if (!bankAccount.getWallet().getId().equals(wallet.getId())) {
                throw new RuntimeException("Bank account does not belong to the specified wallet");
            }

            wallet.setDefaultBankAccount(bankAccount);
            walletRepository.save(wallet);

            LinkBankAccountResponseDto responseDto = new LinkBankAccountResponseDto(
                    wallet.getId(),
                    wallet.getCustomerReference(),
                    bankAccount.getId(),
                    bankAccount.getBankName(),
                    bankAccount.getAccountNumber()
            );

            return ResponseModel.successResponse(responseDto, HttpStatus.OK.value(), "Bank account linked successfully to wallet");
        } catch (Exception ex) {
            return ResponseModel.errorResponse(ex.getMessage(), "Failed to link bank account", HttpStatus.BAD_REQUEST);
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
                    .timeStamp(Instant.now())
                    .build();

            ledgerEntryRepository.save(ledgerEntry);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("newBalance", newBalance);
            responseData.put("withdrawnAmount", withdrawalAmount);
            responseData.put("reference", transaction.getReference());
            responseData.put("bankAccountNumber", wallet.getDefaultBankAccount().getAccountNumber());


            return ResponseModel.successResponse(responseData , HttpStatus.OK.value(), "Withdrawal successful to bank account");

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
