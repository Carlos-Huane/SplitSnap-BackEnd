package com.splitsnap.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor 
public class TransactionHistoryDTO {

    private String id;
    private String type; 
    private String description;
    private String groupName;
    private Double amount;
    private LocalDateTime date; 
}