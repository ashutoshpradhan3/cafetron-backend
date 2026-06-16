package com.cafetron.wallet.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedTransactionDto {

    private List<TransactionResponseDto> transactions;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}