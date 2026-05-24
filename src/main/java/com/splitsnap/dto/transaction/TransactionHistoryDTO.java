package com.splitsnap.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter 
@Builder
@NoArgsConstructor
@AllArgsConstructor // <- IMPORTANTE: Agrega esto para habilitar el constructor manual
public class TransactionHistoryDTO {
    private UUID id;
    private String type;
    private String description;
    private String groupName;
    private Double amount;
    private LocalDateTime date; 
}