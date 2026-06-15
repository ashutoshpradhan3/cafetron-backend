package com.cafetron.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "`Order`")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token")
    private String token;

    @Column(name = "overall_status")
    private String overallStatus;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "location")
    private String location;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "vendor_count")
    private Integer vendorCount;

    @Column(name = "vendor_accepted_count")
    private Integer vendorAcceptedCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

}
