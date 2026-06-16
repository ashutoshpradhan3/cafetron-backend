package com.cafetron.wallet.entity;

import com.cafetron.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;


    @Column(nullable = false,precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt=LocalDateTime.now();
    }
}
