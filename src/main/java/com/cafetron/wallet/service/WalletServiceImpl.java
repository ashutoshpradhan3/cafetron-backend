package com.cafetron.wallet.service;

import com.cafetron.wallet.dto.PagedTransactionDto;
import com.cafetron.wallet.dto.TransactionResponseDto;
import com.cafetron.wallet.dto.WalletResponseDto;
import com.cafetron.wallet.entity.Transaction;
import com.cafetron.wallet.entity.TransactionType;
import com.cafetron.wallet.entity.Wallet;
import com.cafetron.wallet.exception.InsufficientFundsException;
import com.cafetron.wallet.exception.WalletNotFoundException;
import com.cafetron.wallet.repository.TransactionRepository;
import com.cafetron.wallet.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        // 4. build and save a Transaction with type=DEBIT, wallet, amount, description
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Wallet wallet = walletRepository.findByUser_Id(userId).orElseThrow(() -> new WalletNotFoundException(userId));

        if (wallet.getBalance().compareTo(amount) >= 0) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else {
            throw new InsufficientFundsException(wallet.getBalance(), amount);
        }

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(TransactionType.DEBIT);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void refund(Long userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(TransactionType.REFUND);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void topUp(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be positive");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TOP_UP);
        transaction.setDescription("Wallet Top-Up");
        transactionRepository.save(transaction);

    }

    @Override
    public WalletResponseDto getWallet(Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        return WalletResponseDto.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUser().getId())
                .balance(wallet.getBalance())
                .updatedAt(wallet.getUpdatedAt())
                .build();


    }

    @Override
    public PagedTransactionDto getTransactions(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        Page<Transaction> page=transactionRepository.findByWallet_IdOrderByCreatedAtDesc(wallet.getId(),  pageable);
        List<TransactionResponseDto> transactions=page.getContent()
                .stream()
                .map(this::mapToTransactionDto)
                .collect(Collectors.toList());

        return PagedTransactionDto.builder()
                .transactions(transactions)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();


    }


    private TransactionResponseDto mapToTransactionDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .orderId(transaction.getOrder()!= null ?transaction.getOrder().getId():null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
