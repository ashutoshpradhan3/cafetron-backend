package com.cafetron.wallet.dto;

import com.cafetron.wallet.entity.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private Long orderId;
    private LocalDateTime createdAt;
}