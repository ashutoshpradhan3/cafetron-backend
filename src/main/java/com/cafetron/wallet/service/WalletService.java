package com.cafetron.wallet.service;

import java.math.BigDecimal;

public interface WalletService {
    void debit(Long userId, BigDecimal amount, String description);
}