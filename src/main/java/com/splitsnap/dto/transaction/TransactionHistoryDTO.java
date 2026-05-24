package com.splitsnap.dto.transaction;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class TransactionHistoryDTO {
    private String id;
    private String type; // "EXPENSE" o "PAYMENT"
    private String description;
    private String groupName;
    private Double amount;
    private LocalDateTime date;
}