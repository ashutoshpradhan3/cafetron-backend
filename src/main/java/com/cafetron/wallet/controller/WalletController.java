package com.cafetron.wallet.controller;

import com.cafetron.wallet.dto.PagedTransactionDto;
import com.cafetron.wallet.dto.TopUpRequestDto;
import com.cafetron.wallet.dto.WalletResponseDto;
import com.cafetron.security.UserPrincipal;
import com.cafetron.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletResponseDto> getWallet(
            @AuthenticationPrincipal UserPrincipal principal) {

        WalletResponseDto response = walletService.getWallet(requirePrincipal(principal).getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/topup")
    public ResponseEntity<String> topUp(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TopUpRequestDto request) {

        walletService.topUp(requirePrincipal(principal).getId(), request.getAmount());
        return ResponseEntity.ok("Wallet topped up successfully");
    }

    @GetMapping("/transactions")
    public ResponseEntity<PagedTransactionDto> getTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedTransactionDto response = walletService.getTransactions(requirePrincipal(principal).getId(),  pageable);
        return ResponseEntity.ok(response);
    }

    private UserPrincipal requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return principal;
    }
}
