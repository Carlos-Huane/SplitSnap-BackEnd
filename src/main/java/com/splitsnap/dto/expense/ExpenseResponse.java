package com.splitsnap.dto.expense;

import com.splitsnap.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private UUID id;
    private String description;
    private Double amount;
    private UUID groupId;
    private UUID paidBy;
    private LocalDate expenseDate;

    public static ExpenseResponse from(Expense expense) {
        if (expense == null) return null;

        return ExpenseResponse.builder()
                // Convertimos el String ID de la entidad a UUID para el DTO de salida
                .id(expense.getId() != null ? UUID.fromString(expense.getId()) : null)
                .description(expense.getDescription())
                .amount(expense.getAmountAsDouble()) // Usamos el método compatible Double
                .groupId(expense.getGroup() != null ? expense.getGroup().getId() : null)
                .paidBy(expense.getPaidBy() != null ? expense.getPaidBy().getId() : null)
                .expenseDate(expense.getExpenseDate() != null ? expense.getExpenseDate() : 
                             (expense.getCreatedAt() != null ? expense.getCreatedAt().toLocalDate() : LocalDate.now()))
                .build();
    }
}