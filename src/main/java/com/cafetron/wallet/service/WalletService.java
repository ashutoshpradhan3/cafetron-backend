package com.cafetron.wallet.service;

import com.cafetron.wallet.dto.PagedTransactionDto;
import com.cafetron.wallet.dto.WalletResponseDto;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface WalletService {
    void debit(Long userId, BigDecimal amount, String description);
    void refund(Long userId, BigDecimal amount, String description);
    void topUp(Long userId, BigDecimal amount);
    WalletResponseDto getWallet(Long userId);
    PagedTransactionDto getTransactions(Long userId, Pageable pageable);
}