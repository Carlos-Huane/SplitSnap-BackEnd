package com.splitsnap.dto.credit;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class TransactionResponse {
    private Double amount;
    private String type; // PURCHASE o SPEND
    private String debtId; // null si es compra
    private LocalDateTime createdAt;
}