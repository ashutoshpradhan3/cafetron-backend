package com.cafetron.wallet.exception;

public class DuplicateRefundException extends RuntimeException {

    public DuplicateRefundException(Long orderId) {
        super("Refund already processed for order id: " + orderId);
    }
}