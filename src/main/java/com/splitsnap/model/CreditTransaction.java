package com.splitsnap.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_transactions")
@Data
public class CreditTransaction {
    @Id
    private String id; // VARCHAR(36) generado en Java

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double amount;

    private String type; // "PURCHASE" o "SPEND"

    private String debtId; // Referencia opcional si es un gasto

    private LocalDateTime createdAt;
}