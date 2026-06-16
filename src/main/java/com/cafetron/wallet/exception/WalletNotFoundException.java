package com.cafetron.wallet.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Long userId) {
        super("Wallet not found for user id: " + userId);
    }
}