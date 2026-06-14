package com.cafetron.wallet.repository;

import com.cafetron.wallet.entity.Transaction;
import com.cafetron.wallet.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;



public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    Page<Transaction> findByWallet_IdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    List<Transaction> findByWallet_IdOrderByCreatedAtDesc(Long walletId);
    List<Transaction> findByOrderId(Long orderId);
    boolean existsByOrderIdAndType(Long orderId, TransactionType type);
}
