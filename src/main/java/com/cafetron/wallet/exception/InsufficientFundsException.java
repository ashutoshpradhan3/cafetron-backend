package com.cafetron.wallet.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(BigDecimal balance, BigDecimal amount) {
        super("Insufficient funds. Current balance: "
                + balance + ", Required: " + amount);
    }
}