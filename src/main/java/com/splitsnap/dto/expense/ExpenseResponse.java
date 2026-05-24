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
@AllArgsConstructor // Permite que se use tanto con Builder como de forma tradicional
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
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .groupId(expense.getGroup() != null ? expense.getGroup().getId() : null)
                .paidBy(expense.getPaidBy() != null ? expense.getPaidBy().getId() : null)
                // Usamos la fecha del gasto; si no existe, sacamos la parte de la fecha de su creación
                .expenseDate(expense.getExpenseDate() != null ? expense.getExpenseDate() : 
                             (expense.getCreatedAt() != null ? expense.getCreatedAt().toLocalDate() : LocalDate.now()))
                .build();
    }
}