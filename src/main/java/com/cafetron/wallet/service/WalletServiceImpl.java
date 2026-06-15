package com.cafetron.wallet.service;

import com.cafetron.wallet.entity.Transaction;
import com.cafetron.wallet.entity.TransactionType;
import com.cafetron.wallet.entity.Wallet;
import com.cafetron.wallet.repository.TransactionRepository;
import com.cafetron.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void debit(Long userId, BigDecimal amount, String description) {
        // 1. find wallet by userId, throw if not found
        // 2. check balance >= amount, throw IllegalArgumentException if not
        // 3. subtract amount and save wallet
        // 4. build and save a Transaction with type=DEBIT, walletId, amount, description
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();

        if(wallet.getBalance().compareTo(amount) >= 0){
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }
        else{
            throw new IllegalArgumentException("Insufficient Balance");
        }

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(TransactionType.DEBIT);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void refund(Long userId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(TransactionType.REFUND);
        transactionRepository.save(transaction);
    }
}

