package com.cafetron.wallet.controller;

import com.cafetron.wallet.dto.PagedTransactionDto;
import com.cafetron.wallet.dto.TopUpRequestDto;
import com.cafetron.wallet.dto.WalletResponseDto;
import com.cafetron.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletResponseDto> getWallet(
            @RequestParam Long userId) {

        WalletResponseDto response = walletService.getWallet(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/topup")
    public ResponseEntity<String> topUp(
            @Valid @RequestBody TopUpRequestDto request) {

        walletService.topUp(request.getUserId(), request.getAmount());
        return ResponseEntity.ok("Wallet topped up successfully");
    }

    @GetMapping("/transactions")
    public ResponseEntity<PagedTransactionDto> getTransactions(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedTransactionDto response = walletService.getTransactions(userId,  pageable);
        return ResponseEntity.ok(response);
    }
}