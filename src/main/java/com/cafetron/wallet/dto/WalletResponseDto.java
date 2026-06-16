package com.cafetron.wallet.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponseDto {

    private Long walletId;
    private Long userId;
    private BigDecimal balance;
    private LocalDateTime updatedAt;
}