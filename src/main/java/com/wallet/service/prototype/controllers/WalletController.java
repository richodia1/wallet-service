package com.wallet.service.prototype.controllers;

import com.wallet.service.prototype.dtos.request.CreateWalletRequestDto;
import com.wallet.service.prototype.dtos.request.FundWalletRequestDto;
import com.wallet.service.prototype.models.ResponseModel;
import com.wallet.service.prototype.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<ResponseModel> createWallet(@RequestBody CreateWalletRequestDto request) {
        return walletService.createWallet(request);
    }

    @PostMapping("/fund")
    public ResponseEntity<ResponseModel> fundWallet(@RequestBody FundWalletRequestDto requestDto) {
        return walletService.fundWallet(requestDto);
    }
    @PostMapping("/withdraw")
    public ResponseEntity<ResponseModel> withdrawFromWallet(@RequestBody FundWalletRequestDto requestDto) {
        return walletService.withdrawFromWalletToBankAccount(requestDto);
    }

    @GetMapping("/customer/{customerReference}")
    public ResponseEntity<ResponseModel> getWalletByCustomerReference(@PathVariable String customerReference) {
        return walletService.getWalletByCustomerReference(customerReference);
    }
    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<ResponseModel> getWalletTransactionHistory(@PathVariable UUID walletId) {
        return walletService.getWalletTransactionHistory(walletId);
    }
    @GetMapping("/{walletId}/ledger")
    public ResponseEntity<ResponseModel> getLedgerEntries(@PathVariable UUID walletId) {
        return walletService.getLedgerEntriesByWalletId(walletId);
    }

    @GetMapping("/customer/{customerReference}/default-bank-account")
    public ResponseEntity<ResponseModel> getDefaultBankAccount(@PathVariable String customerReference) {
        return walletService.getDefaultBankAccountIdResponse(customerReference);
    }
}